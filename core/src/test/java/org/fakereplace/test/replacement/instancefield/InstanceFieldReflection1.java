package org.fakereplace.test.replacement.instancefield;

public class InstanceFieldReflection1
{

   String value = "hi";

   public int vis;
   @SomeAnnotation
   private int hid;

   public String getValue()
   {
      return value;
   }

}
