package org.fakereplace;

public class BuiltinClassData
{

   static final String[] doNotInstrument = { "org/fakereplace", "java/lang", "sun/reflect/generics", "sun/reflect/annotation", "java/io", "java/math", "java/util/concurrent", "java/util/Currency", "sun/reflect/Unsafe", "java/util/Random", "sun/misc/Unsafe" };

   public static boolean skipInstrumentation(String className)
   {
      boolean doNot = false;
      for (String s : doNotInstrument)
      {
         if (className.startsWith(s))
         {
            doNot = true;
            break;
         }
      }
      return doNot && !className.startsWith("org/fakereplace/test");
   }
}
