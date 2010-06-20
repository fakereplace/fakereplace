package org.fakereplace.test.replacement.instancefield;

public class InstanceToStatic1
{
   private static int field = 20;

   public int getField()
   {
      return field;
   }

   public void setField(int field)
   {
      this.field = field;
   }

}
