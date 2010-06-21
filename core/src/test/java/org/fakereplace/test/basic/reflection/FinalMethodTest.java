package org.fakereplace.test.basic.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

public class FinalMethodTest
{
   @Test
   public void testFinalMethodModifiers() throws SecurityException, NoSuchMethodException
   {
      Method m = ClassWithFinalMethods.class.getMethod("method");
      assert (m.getModifiers() & Modifier.FINAL) != 0;
   }
}
