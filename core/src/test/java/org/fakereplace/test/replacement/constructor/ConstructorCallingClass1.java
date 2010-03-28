package org.fakereplace.test.replacement.constructor;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class ConstructorCallingClass1
{

   public static ConstructorClass1 getInstance()
   {
      return new ConstructorClass1("b");
   }
}
