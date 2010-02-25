package org.fakereplace.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javassist.bytecode.AccessFlag;

import org.fakereplace.util.DescriptorUtils;

/**
 * Class that holds information about a method.
 * 
 * @author stuart
 * 
 */
public class MethodData
{
   String methodName;
   String descriptor;

   /**
    * contains the argument portion of the descriptor minus the return type
    */
   String argumentDescriptor;
   /**
    * the return type of the method
    */
   String returnTypeDescriptor;

   MemberType type;
   int accessFlags;

   public MethodData(String name, String descriptor)
   {
      this.methodName = name;
      this.descriptor = descriptor;
      this.returnTypeDescriptor = DescriptorUtils.getReturnType(descriptor);
      this.argumentDescriptor = DescriptorUtils.getArgumentString(descriptor);
   }

   /**
    * The actual class that the method resides in java not internal format
    */
   String className;

   public String getClassName()
   {
      return className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   public int getAccessFlags()
   {
      return accessFlags;
   }

   public void setAccessFlags(int accessFlags)
   {
      this.accessFlags = accessFlags;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public String getDescriptor()
   {
      return descriptor;
   }

   public MemberType getType()
   {
      return type;
   }

   public void setType(MemberType type)
   {
      this.type = type;
   }

   public boolean isStatic()
   {
      return (accessFlags & AccessFlag.STATIC) != 0;
   }

   public String getArgumentDescriptor()
   {
      return argumentDescriptor;
   }

   public Method getMethod(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchMethodException
   {
      Class<?>[] methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
      Method method = actualClass.getMethod(methodName, methodDesc);
      return method;
   }

   /**
    * If this method is actually a constructor get the construtor object
    */
   public Constructor getConstructor(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchMethodException
   {
      Class<?>[] methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
      Constructor method = actualClass.getConstructor(methodDesc);
      return method;
   }

}
