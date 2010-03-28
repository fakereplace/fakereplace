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
   final String methodName;
   final String descriptor;

   /**
    * contains the argument portion of the descriptor minus the return type
    */
   final String argumentDescriptor;
   /**
    * the return type of the method
   final  */
   String returnTypeDescriptor;

   final MemberType type;
   final int accessFlags;

   public MethodData(String name, String descriptor, String className, MemberType type, int accessFlags)
   {
      this.methodName = name;
      this.descriptor = descriptor;
      this.returnTypeDescriptor = DescriptorUtils.getReturnType(descriptor);
      this.argumentDescriptor = DescriptorUtils.getArgumentString(descriptor);
      this.className = className;
      this.type = type;
      this.accessFlags = accessFlags;
   }

   /**
    * The actual class that the method resides in java not internal format
    */
   final String className;

   public String getClassName()
   {
      return className;
   }

   public int getAccessFlags()
   {
      return accessFlags;
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
      Class<?>[] methodDesc;
      if (type == MemberType.FAKE && !isStatic())
      {
         methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
         Class<?>[] ret = new Class<?>[methodDesc.length + 1];
         ret[0] = ClassDataStore.getRealClassFromProxyName(actualClass.getName());
         for (int i = 0; i < methodDesc.length; ++i)
         {
            ret[i + 1] = methodDesc[i];
         }
         methodDesc = ret;
      }
      else
      {
         methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
      }
      Method method = actualClass.getDeclaredMethod(methodName, methodDesc);
      return method;

   }

   /**
    * If this method is actually a constructor get the construtor object
    */
   public Constructor getConstructor(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchMethodException
   {
      Class<?>[] methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
      Constructor method = actualClass.getDeclaredConstructor(methodDesc);
      return method;
   }

}
