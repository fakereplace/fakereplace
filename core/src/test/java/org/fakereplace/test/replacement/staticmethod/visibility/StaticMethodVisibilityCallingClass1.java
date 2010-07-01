package org.fakereplace.test.replacement.staticmethod.visibility;

import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass1;

public class StaticMethodVisibilityCallingClass1
{
   public static String callingClass()
   {
      return StaticMethodVisibilityClass1.method();
   }
}
