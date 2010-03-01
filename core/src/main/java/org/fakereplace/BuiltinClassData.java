package org.fakereplace;

public class BuiltinClassData
{

   static final String[] doNotInstrument = { "org/fakereplace", "java/lang", "sun/reflect/generics", "sun/reflect/annotation", "java/io", "java/math", "java/util/concurrent", "java/util/Currency", "sun/reflect/Unsafe", "java/util/Random", "sun/misc/Unsafe" };

   static final String[] exceptions = { "java/lang/reflect/Proxy", "org/fakereplace/test" };

   public static boolean skipInstrumentation(String className)
   {
      for (String s : exceptions)
      {
         if (className.startsWith(s))
         {
            return false;
         }
      }
      for (String s : doNotInstrument)
      {
         if (className.startsWith(s))
         {
            return true;
         }
      }
      return false;
   }
}
