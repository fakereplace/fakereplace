package org.fakereplace.manip;

/**
 * Stores information about an added instance field.
 * 
 * @author stuart
 * 
 */
public class AddedFieldData
{
   final int arrayIndex;
   final String name;
   final String descriptor;
   final String className;

   public AddedFieldData(int arrayIndex, String name, String descriptor, String className)
   {
      super();
      this.arrayIndex = arrayIndex;
      this.name = name;
      this.descriptor = descriptor;
      this.className = className;
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

}
