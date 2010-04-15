package org.fakereplace.detector;

public class NewClassData
{

   NewClassData(String className, ClassLoader classLoader)
   {
      this.classLoader = classLoader;
      this.className = className;
   }

   final String className;
   final ClassLoader classLoader;
   Class<?> javaClass;

   public String getClassName()
   {
      return className;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public Class<?> getJavaClass()
   {
      if (javaClass == null)
      {
         try
         {
            javaClass = classLoader.loadClass(className);
         }
         catch (ClassNotFoundException e)
         {

         }
      }
      return javaClass;
   }

}
