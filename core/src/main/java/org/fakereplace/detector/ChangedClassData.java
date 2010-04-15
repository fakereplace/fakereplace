package org.fakereplace.detector;

public class ChangedClassData
{

   public ChangedClassData(Class<?> javaClass, byte[] classFile)
   {
      this.javaClass = javaClass;
      this.classFile = classFile;
   }

   final Class<?> javaClass;
   final byte[] classFile;

   public Class<?> getJavaClass()
   {
      return javaClass;
   }

   public byte[] getClassFile()
   {
      return classFile;
   }

}
