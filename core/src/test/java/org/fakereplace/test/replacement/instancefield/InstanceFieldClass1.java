package org.fakereplace.test.replacement.instancefield;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class InstanceFieldClass1
{

   private int value = 0;

   public int get()
   {
      return value;
   }

   public void inc()
   {
      value++;
   }
}
