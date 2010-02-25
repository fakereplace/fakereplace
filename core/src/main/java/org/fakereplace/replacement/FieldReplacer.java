package org.fakereplace.replacement;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;

import org.fakereplace.Transformer;
import org.fakereplace.boot.Constants;
import org.fakereplace.boot.GlobalClassDefinitionData;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.MemberType;

public class FieldReplacer
{

   public static void handleFieldReplacement(ClassFile file, ClassLoader loader, Class oldClass)
   {

      ClassData data = ClassDataStore.getClassData(loader, Descriptor.toJvmName(file.getName()));

      Set<FieldData> fields = new HashSet<FieldData>();
      fields.addAll(data.getFields());

      ListIterator<?> it = file.getFields().listIterator();

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
            break;
         }
         // This is a newly added field.
         if (md == null)
         {
            if ((m.getAccessFlags() & AccessFlag.STATIC) != 0)
            {
               addStaticField(file, loader, m, data);
            }
            // TODO deal with non static fields
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
   private static void addStaticField(ClassFile file, ClassLoader loader, FieldInfo m, ClassData data)
   {
      // this is quite simple. First we create a proxy
      String proxyName = GlobalClassDefinitionData.getProxyName();
      ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
      proxy.setAccessFlags(AccessFlag.PUBLIC);
      FieldInfo newField = new FieldInfo(proxy.getConstPool(), m.getName(), m.getDescriptor());
      newField.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
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
         data.addField(newField, MemberType.FAKE);
      }
      catch (DuplicateMemberException e)
      {
         // can't happen
      }
   }

}
