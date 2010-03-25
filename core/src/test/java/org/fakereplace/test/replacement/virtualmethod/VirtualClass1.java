package org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class VirtualClass1
{

   private Integer value = 0;

   public Integer getValue()
   {
      addValue(1);
      return value;
   }

   public void addValue(int value)
   {
      this.value = this.value + value;
   }
}
