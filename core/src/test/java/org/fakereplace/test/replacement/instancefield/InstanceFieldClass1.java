package org.fakereplace.test.replacement.instancefield;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class InstanceFieldClass1
{

   private int value = 0;

   private long lv = 1;

   public int get()
   {
      return value;
   }

   public void inc()
   {
      value++;
   }

   public long getlong()
   {
      return lv;
   }

   public void inclong()
   {
      lv++;
   }
}
