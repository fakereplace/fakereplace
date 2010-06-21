package org.fakereplace.test.replacement.finalmethod;

public class FinalMethodClass1
{
   public String finalMethod()
   {
      return "finalMethod-replaced";
   }

   public final String nonFinalMethod()
   {
      return "nonFinalMethod-replaced";
   }

   public final String addedFinalMethod()
   {
      return "nonFinalMethod";
   }
}
