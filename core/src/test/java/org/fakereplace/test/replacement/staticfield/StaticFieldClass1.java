package org.fakereplace.test.replacement.staticfield;

import java.util.List;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class StaticFieldClass1
{
   public static long longField = 0;

   static List<String> list = null;

   public static long incAndGet()
   {
      longField++;
      return longField;
   }

}
