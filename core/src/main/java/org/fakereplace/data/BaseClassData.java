package org.fakereplace.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import org.fakereplace.boot.Constants;

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
      Set<MethodData> meths = new HashSet<MethodData>();
      for (Object o : file.getMethods())
      {
         MethodInfo m = (MethodInfo) o;
         MemberType type = MemberType.NORMAL;
         if ((m.getDescriptor().equals(Constants.ADDED_METHOD_DESCRIPTOR) && m.getName().equals(Constants.ADDED_METHOD_NAME)) || (m.getDescriptor().equals(Constants.ADDED_STATIC_METHOD_DESCRIPTOR) && m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME)) || (m.getDescriptor().equals(Constants.ADDED_CONSTRUCTOR_DESCRIPTOR)))
         {
            type = MemberType.ADDED_SYSTEM;
         }

         MethodData md = new MethodData(m.getName(), m.getDescriptor(), file.getName(), type, m.getAccessFlags());
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
