package org.fakereplace.test.replacement.staticfield;

import java.util.List;

public class StaticFieldClass1
{
   public static long longField = 0;

   public static List<String> list = null;

   public static long incAndGet()
   {
      longField++;
      return longField;
   }

}
