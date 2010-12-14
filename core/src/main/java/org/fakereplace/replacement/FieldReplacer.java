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
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.SignatureAttribute;

import org.fakereplace.Transformer;
import org.fakereplace.boot.ProxyDefinitionStore;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataBuilder;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.FieldReferenceDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.manip.data.AddedFieldData;
import org.fakereplace.manip.staticfield.StaticFieldClassFactory;
import org.fakereplace.reflection.FieldAccessor;

public class FieldReplacer
{

   public static void handleFieldReplacement(ClassFile file, ClassLoader loader, Class<?> oldClass, ClassDataBuilder builder)
   {

      BaseClassData data = builder.getBaseData();

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
            else if (i.getName().equals(m.getName()) && i.getType().equals(m.getDescriptor()))
            {
               // we have a field whoes access modifiers have changed.
               if ((i.getAccessFlags() | AccessFlag.STATIC) == (m.getAccessFlags() | AccessFlag.STATIC))
               {
                  // change from / to static can be handled fine
               }
            }
         }
         // This is a newly added field.
         if (md == null)
         {
            if ((m.getAccessFlags() & AccessFlag.STATIC) != 0)
            {
               addStaticField(file, loader, m, builder, oldClass);
            }
            else
            {
               int fieldNo = addInstanceField(file, loader, m, builder, oldClass);
               addedFields.add(new AddedFieldData(fieldNo, m.getName(), m.getDescriptor(), file.getName(), loader));
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
         if (md.getMemberType() == MemberType.NORMAL)
         {
            FieldInfo old = new FieldInfo(file.getConstPool(), md.getName(), md.getType());
            old.setAccessFlags(md.getAccessFlags());
            builder.removeField(md);
            try
            {
               Field field = md.getField(oldClass);
               file.addField(old);
               old.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), field));
            }
            catch (DuplicateMemberException e)
            {
               // this should not happen
               throw new RuntimeException(e);
            }
            catch (SecurityException e)
            {
               throw new RuntimeException(e);
            }
            catch (ClassNotFoundException e)
            {
               throw new RuntimeException(e);
            }
            catch (NoSuchFieldException e)
            {
               throw new RuntimeException(e);
            }
         }
      }

      for (AddedFieldData a : addedFields)
      {
         Transformer.getManipulator().rewriteInstanceFieldAccess(a);
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
   private static void addStaticField(ClassFile file, ClassLoader loader, FieldInfo m, ClassDataBuilder builder, Class<?> oldClass)
   {
      // this will generate the class holding the satic field if is does not
      // already exist. This allows
      // the static field to hold its value accross multiple replacements

      String sig = null;
      SignatureAttribute sat = (SignatureAttribute) m.getAttribute(SignatureAttribute.tag);
      if (sat != null)
      {
         sig = sat.getSignature();
      }

      String proxyName = StaticFieldClassFactory.getStaticFieldClass(oldClass, m.getName(), m.getDescriptor(), sig);
      try
      {
         Field fieldFromProxy = loader.loadClass(proxyName).getDeclaredField(m.getName());
         AnnotationDataStore.recordFieldAnnotations(fieldFromProxy, (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag));
      }
      catch (Exception e)
      {
         // should not happen
         e.printStackTrace();
      }
      Transformer.getManipulator().rewriteStaticFieldAccess(file.getName(), proxyName, m.getName(), loader);
      builder.addFakeField(m, proxyName, m.getAccessFlags());

   }

   /**
    * This will create a proxy with a non static field. This field does not
    * store anything, it merely provides a Field object for reflection. Attempts
    * to change and read it's value are redirected to the actual array based
    * store
    * 
    * @param file
    * @param loader
    * @param m
    * @param data
    */
   private static int addInstanceField(ClassFile file, ClassLoader loader, FieldInfo m, ClassDataBuilder builder, Class<?> oldClass)
   {
      String sig = null;
      SignatureAttribute sigat = (SignatureAttribute) m.getAttribute(SignatureAttribute.tag);
      if (sigat != null)
      {
         sig = sigat.getSignature();
      }
      int fieldNo = FieldReferenceDataStore.getFieldNo(m.getName(), m.getDescriptor(), sig);
      String proxyName = ProxyDefinitionStore.getProxyName();
      ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
      ClassDataStore.registerProxyName(oldClass, proxyName);
      FieldAccessor accessor = new FieldAccessor(oldClass, fieldNo);
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
         ProxyDefinitionStore.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
         builder.addFakeField(newField, proxyName, m.getAccessFlags());
      }
      catch (DuplicateMemberException e)
      {
         // can't happen
      }
      return fieldNo;
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
