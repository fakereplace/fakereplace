package org.fakereplace.manip.data;

import org.fakereplace.manip.util.ClassloaderFiltered;

public class SubclassVirtualCallData implements ClassloaderFiltered<SubclassVirtualCallData>
{

   private final ClassLoader classLoader;
   private final String className;
   private final String methodName;
   private final String methodDescriptor;
   private final String subclassName;

   public SubclassVirtualCallData(ClassLoader classLoader, String className, String methodName, String methodDescriptor, String subclassName)
   {
      this.classLoader = classLoader;
      this.className = className;
      this.methodName = methodName;
      this.methodDescriptor = methodDescriptor;
      this.subclassName = subclassName;
   }

   public SubclassVirtualCallData getInstane()
   {
      return this;
   }

   public String getMethodName()
   {
      return methodName;
   }

   public String getClassName()
   {
      return className;
   }

   public String getSubclassName()
   {
      return subclassName;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public String getMethodDescriptor()
   {
      return methodDescriptor;
   }

}
