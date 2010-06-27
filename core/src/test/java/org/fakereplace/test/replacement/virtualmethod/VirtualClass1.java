package org.fakereplace.test.replacement.virtualmethod;

import java.util.List;

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

   public void addValue(int value) throws ArithmeticException
   {
      this.value = this.value + value;
   }

   public List<String> getStuff(List<Integer> aList)
   {
      return null;
   }

   private void privateFunction()
   {

   }

   @Override
   public String toString()
   {
      return "VirtualChild1";
   }
}
