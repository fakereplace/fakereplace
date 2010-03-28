package org.fakereplace.test.replacement.constructor;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class ConstructorClass1
{

   public ConstructorClass1(String a)
   {
      value = a;
   }

   String value = "a";

   public String getValue()
   {
      return value;
   }
}
