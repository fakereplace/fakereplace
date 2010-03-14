package org.fakereplace.test.replacement.instancefield;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class InstanceFieldClass1
{

   int afield;
   int bfield;

   public InstanceFieldClass1()
   {
      // sv = "aa";
   }

   public String sv;

   int yy;

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

   public String getSv()
   {
      return null;
   }

}
