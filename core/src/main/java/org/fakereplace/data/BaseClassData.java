package org.fakereplace.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import org.fakereplace.boot.Constants;
import org.fakereplace.util.DescriptorUtils;

/**
 * This class holds everything there is to know about a class that has been seen
 * by the transformer. This stores the information about the original class, not
 * about any modifications
 * 
 * @author stuart
 * 
 */
public class BaseClassData
{

   private final String className;
   private final String internalName;
   private final Set<MethodData> methods;
   private final Set<FieldData> fields;
   private final ClassLoader loader;
   private final String superClassName;

   public BaseClassData(ClassFile file, ClassLoader loader)
   {
      className = file.getName();
      internalName = Descriptor.toJvmName(file.getName());
      this.loader = loader;
      superClassName = Descriptor.toJvmName(file.getSuperclass());
      boolean finalMethod = false;
      Set<MethodData> meths = new HashSet<MethodData>();
      for (Object o : file.getMethods())
      {
         String methodClassName = className;
         MethodInfo m = (MethodInfo) o;
         MemberType type = MemberType.NORMAL;
         if ((m.getDescriptor().equals(Constants.ADDED_METHOD_DESCRIPTOR) && m.getName().equals(Constants.ADDED_METHOD_NAME)) || (m.getDescriptor().equals(Constants.ADDED_STATIC_METHOD_DESCRIPTOR) && m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME)) || (m.getDescriptor().equals(Constants.ADDED_CONSTRUCTOR_DESCRIPTOR)))
         {
            type = MemberType.ADDED_SYSTEM;
         }
         else if (m.getAttribute(Constants.ADDED_SUPERCLASS_DELEGATING_METHOD_ATTRIBUTE) != null)
         {
            AttributeInfo at = m.getAttribute(Constants.ADDED_SUPERCLASS_DELEGATING_METHOD_ATTRIBUTE);
            type = MemberType.ADDED_DELEGATE;
            // we want the class name to be the class that is being delegated to
            methodClassName = new String(at.get());
         }
         else if (m.getAttribute(Constants.ADDED_SUPERCLASS_DELEGATING_METHOD_ATTRIBUTE) != null)
         {
            AttributeInfo at = m.getAttribute(Constants.ADDED_SUPERCLASS_DELEGATING_METHOD_ATTRIBUTE);
            type = MemberType.ADDED_DELEGATE;
            // we want the class name to be the class that is being delegated to
            methodClassName = new String(at.get());
         }
         else if (m.getAttribute(Constants.FINAL_METHOD_ATTRIBUTE) != null)
         {
            finalMethod = true;
         }

         MethodData md = new MethodData(m.getName(), m.getDescriptor(), methodClassName, type, m.getAccessFlags(), finalMethod);
         meths.add(md);
      }
      this.methods = Collections.unmodifiableSet(meths);
      Set<FieldData> fieldData = new HashSet<FieldData>();
      for (Object o : file.getFields())
      {
         FieldInfo m = (FieldInfo) o;
         MemberType mt = MemberType.NORMAL;
         if (m.getName().equals(Constants.ADDED_FIELD_NAME))
         {
            mt = MemberType.ADDED_SYSTEM;
         }
         fieldData.add(new FieldData(m, mt, className));
      }
      this.fields = Collections.unmodifiableSet(fieldData);
   }

   public BaseClassData(Class<?> cls)
   {
      className = cls.getName();
      internalName = Descriptor.toJvmName(cls.getName());
      this.loader = cls.getClassLoader();
      if (cls.getSuperclass() != null)
      {
         superClassName = Descriptor.toJvmName(cls.getSuperclass().getName());
      }
      else
      {
         superClassName = null;
      }
      Set<MethodData> meths = new HashSet<MethodData>();
      for (Method m : cls.getMethods())
      {
         MemberType type = MemberType.NORMAL;
         MethodData md = new MethodData(m.getName(), DescriptorUtils.getDescriptor(m), cls.getName(), type, m.getModifiers(), false);
         meths.add(md);
      }
      this.methods = Collections.unmodifiableSet(meths);
      Set<FieldData> fieldData = new HashSet<FieldData>();
      for (Field m : cls.getFields())
      {
         fieldData.add(new FieldData(m));
      }
      this.fields = Collections.unmodifiableSet(fieldData);
   }

   /**
    * Searches through parent classloaders of the classes class loader to find
    * the ClassData structure for the super class
    * 
    * @return
    */
   public BaseClassData getSuperClassInformation()
   {
      if (superClassName == null)
      {
         return null;
      }
      BaseClassData superClassInformation = ClassDataStore.getBaseClassData(loader, superClassName);
      ClassLoader l = loader;
      while (superClassInformation == null && l != null)
      {
         l = l.getParent();
         superClassInformation = ClassDataStore.getBaseClassData(l, superClassName);
      }
      return superClassInformation;
   }

   public String getSuperClassName()
   {
      return superClassName;
   }

   public ClassLoader getLoader()
   {
      return loader;
   }

   public String getClassName()
   {
      return className;
   }

   public String getInternalName()
   {
      return internalName;
   }

   public Collection<MethodData> getMethods()
   {
      return methods;
   }

   public Collection<FieldData> getFields()
   {
      return fields;
   }

}
