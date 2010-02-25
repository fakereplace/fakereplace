package org.fakereplace.test.replacement.staticfield;

public class StaticFieldClass1
{
   public static long longField = 0;

   public static long incAndGet()
   {
      longField++;
      return longField;
   }

}
