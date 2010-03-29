package org.fakereplace.test.replacement.constructor;

import java.util.List;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class ConstructorClass1
{

   public ConstructorClass1(String a)
   {
      value = a;
   }

   public ConstructorClass1(List<String> a)
   {
      value = "h";
   }

   String value = "a";

   public String getValue()
   {
      return value;
   }
}
