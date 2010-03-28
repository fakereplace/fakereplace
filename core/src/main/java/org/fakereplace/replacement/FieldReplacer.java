package org.fakereplace.replacement;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.SignatureAttribute;

import org.fakereplace.Transformer;
import org.fakereplace.boot.Constants;
import org.fakereplace.boot.GlobalClassDefinitionData;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.MemberType;
import org.fakereplace.manip.AddedFieldData;
import org.fakereplace.manip.Boxing;
import org.fakereplace.reflection.FieldAccessor;

public class FieldReplacer
{

   public static void handleFieldReplacement(ClassFile file, ClassLoader loader, Class oldClass)
   {

      try
      {
         if ((file.getAccessFlags() & AccessFlag.INTERFACE) == 0)
         {
            FieldInfo m = new FieldInfo(file.getConstPool(), Constants.ADDED_FIELD_NAME, Constants.ADDED_FIELD_DESCRIPTOR);
            m.setAccessFlags(0 | AccessFlag.PUBLIC);
            Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
            b.add(Bytecode.ACONST_NULL);
            b.add(Bytecode.ARETURN);
            file.addField(m);
         }
      }
      catch (DuplicateMemberException e)
      {
         // e.printStackTrace();
      }

      ClassData data = ClassDataStore.getClassData(loader, Descriptor.toJvmName(file.getName()));

      Set<FieldData> fields = new HashSet<FieldData>();
      fields.addAll(data.getFields());

      ListIterator<?> it = file.getFields().listIterator();

      int noAddedFields = 0;
      List<AddedFieldData> addedFields = new ArrayList<AddedFieldData>();

      // now we iterator through all fields
      // in the process we modify the new class so that is's signature
      // is exactly compatible with the old class, otherwise an
      // IncompatibleClassChange exception will be thrown

      while (it.hasNext())
      {
         FieldInfo m = (FieldInfo) it.next();
         FieldData md = null;
         for (FieldData i : fields)
         {

            if (i.getName().equals(m.getName()) && i.getType().equals(m.getDescriptor()) && i.getAccessFlags() == m.getAccessFlags())
            {
               try
               {
                  Field field = i.getField(oldClass);
                  AnnotationDataStore.recordFieldAnnotations(field, (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag));
                  // now revert the annotations:
                  m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), field));
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
               md = i;
               break;
            }
         }
         // we do not need to deal with these
         if (m.getName().equals(Constants.ADDED_FIELD_NAME))
         {
            it.remove();
            continue;
         }
         // This is a newly added field.
         if (md == null)
         {
            if ((m.getAccessFlags() & AccessFlag.STATIC) != 0)
            {
               addStaticField(file, loader, m, data, oldClass);
            }
            else
            {
               addedFields.add(new AddedFieldData(noAddedFields, m.getName(), m.getDescriptor(), file.getName()));
               addInstanceField(file, loader, m, data, oldClass, noAddedFields);
               noAddedFields++;
            }
            it.remove();
         }
         else
         {
            fields.remove(md);
         }
      }
      // these fields have been removed,
      // TODO: rewrite classes that access them to throw a NoSuchFieldError
      for (FieldData md : fields)
      {
         FieldInfo old = new FieldInfo(file.getConstPool(), md.getName(), md.getType());
         old.setAccessFlags(md.getAccessFlags());
         try
         {
            file.addField(old);
         }
         catch (DuplicateMemberException e)
         {
            // this should not happen
            throw new RuntimeException(e);
         }
      }

      // if we have added instance fields we need to instrument all the
      // constructors to create the
      // arrays fist thing
      if (noAddedFields > 0)
      {
         try
         {
            instrumentConstructors(file, addedFields);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      for (AddedFieldData a : addedFields)
      {
         Transformer.getManipulator().rewriteInstanceFieldAccess(a);
      }
   }

   /**
    * initilizes the generated field straight after the call to super() or
    * this()
    * 
    * @param file
    * @param addedFields
    * @throws BadBytecode
    */
   private static void instrumentConstructors(ClassFile file, List<AddedFieldData> addedFields) throws BadBytecode
   {
      int addedFieldsLengthIndex = file.getConstPool().addIntegerInfo(addedFields.size());
      for (Object mo : file.getMethods())
      {
         MethodInfo m = (MethodInfo) mo;
         if (m.getName().equals("<init>"))
         {
            // we want to add:
            // if(added_field == null) added_field = new Object[size];

            // create the body of the conditional first
            Bytecode cond = new Bytecode(file.getConstPool());
            // push this onto the stack
            cond.addAload(0);
            // put the array size on the stack
            cond.addLdc(addedFieldsLengthIndex);
            // create the array
            cond.addAnewarray("java.lang.Object");
            cond.add(Opcode.DUP_X1);
            cond.addPutfield(file.getName(), Constants.ADDED_FIELD_NAME, Constants.ADDED_FIELD_DESCRIPTOR);
            for (int i = 0; i < addedFields.size(); ++i)
            {
               // duplicate the array
               cond.add(Opcode.DUP);
               AddedFieldData d = addedFields.get(i);
               // push the index
               cond.addIconst(i);
               // now push the value
               if (d.getDescriptor().length() > 1)
               {
                  cond.add(Opcode.ACONST_NULL);
               }
               else
               {
                  char c = d.getDescriptor().charAt(0);
                  if (c == 'J')
                  {
                     cond.add(Opcode.LCONST_0);
                  }
                  else if (c == 'F')
                  {
                     cond.add(Opcode.FCONST_0);
                  }
                  else if (c == 'D')
                  {
                     cond.add(Opcode.DCONST_0);
                  }
                  else
                  {
                     cond.add(Opcode.ICONST_0);
                  }
                  Boxing.box(cond, c);
               }
               cond.add(Opcode.AASTORE);
            }
            cond.add(Opcode.POP);
            Bytecode b = new Bytecode(file.getConstPool());
            b.addAload(0);
            b.addGetfield(file.getName(), Constants.ADDED_FIELD_NAME, Constants.ADDED_FIELD_DESCRIPTOR);
            b.add(Opcode.IFNONNULL);
            ByteUtils.add16bit(b, cond.getSize() + 3);
            CodeIterator it = m.getCodeAttribute().iterator();
            it.skipConstructor();
            // built up the bytecode to insert
            // if we try and insert one after the other weirdness happens
            // not sure why
            byte[] bcd = new byte[b.getSize() + cond.getSize()];
            int count = 0;
            for (int i = 0; i < b.getSize(); ++i)
            {
               bcd[i] = b.get()[i];
               count++;
            }
            for (int i = 0; i < cond.getSize(); ++i)
            {
               bcd[count] = cond.get()[i];
               count++;
            }
            it.insert(bcd);
            m.getCodeAttribute().computeMaxStack();
         }
      }
   }

   /**
    * This will create a proxy with a static field, and all access to the static
    * field is re-written to the proxy instead
    * 
    * @param file
    * @param loader
    * @param m
    * @param data
    */
   private static void addStaticField(ClassFile file, ClassLoader loader, FieldInfo m, ClassData data, Class<?> oldClass)
   {
      // this is quite simple. First we create a proxy
      String proxyName = GlobalClassDefinitionData.getProxyName();
      ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
      ClassDataStore.registerProxyName(oldClass, proxyName);
      proxy.setAccessFlags(AccessFlag.PUBLIC);
      FieldInfo newField = new FieldInfo(proxy.getConstPool(), m.getName(), m.getDescriptor());
      newField.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);

      copyFieldAttributes(m, newField);

      try
      {
         proxy.addField(newField);
         Transformer.getManipulator().rewriteStaticFieldAccess(file.getName(), proxyName, m.getName());
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bytes);
         try
         {
            proxy.write(dos);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
         GlobalClassDefinitionData.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
         data.addField(newField, MemberType.FAKE, proxyName);
      }
      catch (DuplicateMemberException e)
      {
         // can't happen
      }
   }

   /**
    * This will create a proxy with a non static field. This field does not store anything, it
    * merely provides a Field object for reflection. Attempts to change and read it's value are 
    * redirected to the actual array based store
    * 
    * @param file
    * @param loader
    * @param m
    * @param data
    */
   private static void addInstanceField(ClassFile file, ClassLoader loader, FieldInfo m, ClassData data, Class<?> oldClass, int arrayPosition)
   {
      String proxyName = GlobalClassDefinitionData.getProxyName();
      ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
      ClassDataStore.registerProxyName(oldClass, proxyName);
      FieldAccessor accessor = new FieldAccessor(oldClass, arrayPosition);
      ClassDataStore.registerFieldAccessor(proxyName, accessor);
      proxy.setAccessFlags(AccessFlag.PUBLIC);
      FieldInfo newField = new FieldInfo(proxy.getConstPool(), m.getName(), m.getDescriptor());
      newField.setAccessFlags(m.getAccessFlags());

      copyFieldAttributes(m, newField);

      try
      {
         proxy.addField(newField);
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bytes);
         try
         {
            proxy.write(dos);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
         GlobalClassDefinitionData.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
         data.addField(newField, MemberType.FAKE, proxyName);
      }
      catch (DuplicateMemberException e)
      {
         // can't happen
      }
   }

   public static void copyFieldAttributes(FieldInfo oldField, FieldInfo newField)
   {
      AnnotationsAttribute annotations = (AnnotationsAttribute) oldField.getAttribute(AnnotationsAttribute.visibleTag);
      SignatureAttribute sigAt = (SignatureAttribute) oldField.getAttribute(SignatureAttribute.tag);

      if (annotations != null)
      {
         AttributeInfo newAnnotations = annotations.copy(newField.getConstPool(), Collections.EMPTY_MAP);
         newField.addAttribute(newAnnotations);
      }
      if (sigAt != null)
      {
         AttributeInfo newAnnotations = sigAt.copy(newField.getConstPool(), Collections.EMPTY_MAP);
         newField.addAttribute(newAnnotations);
      }

   }

}
