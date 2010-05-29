package org.fakereplace.manip.data;

/**
 * Stores information about an added instance field.
 * 
 * @author stuart
 * 
 */
public class AddedFieldData
{
   final private int arrayIndex;
   final private String name;
   final private String descriptor;
   final private String className;
   final private ClassLoader classLoader;

   public AddedFieldData(int arrayIndex, String name, String descriptor, String className, ClassLoader classLoader)
   {
      super();
      this.arrayIndex = arrayIndex;
      this.name = name;
      this.descriptor = descriptor;
      this.className = className;
      this.classLoader = classLoader;
   }

   public int getArrayIndex()
   {
      return arrayIndex;
   }

   public String getName()
   {
      return name;
   }

   public String getDescriptor()
   {
      return descriptor;
   }

   public String getClassName()
   {
      return className;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

}
