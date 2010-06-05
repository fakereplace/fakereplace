package org.fakereplace.test.basic.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fakereplace.boot.Constants;
import org.fakereplace.util.NoInstrument;
import org.testng.annotations.Test;

public class MethodTest
{
   @Test
   public void testDelegatorMethodAdded() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException
   {
      TestRunner.runTest();
   }

   @Test
   public void testGetDeclaredMethods()
   {
      DoStuff d = new DoStuff();
      Method[] meths = d.getClass().getDeclaredMethods();
      for (Method m : meths)
      {
         if (m.getName().equals(Constants.ADDED_METHOD_NAME) || m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME))
         {
            throw new RuntimeException("Added method delegator showing up in declared methods");
         }
      }
   }

   @NoInstrument
   private static class TestRunner
   {
      public static void runTest() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
      {
         DoStuff d = new DoStuff();
         Method m = d.getClass().getMethod(Constants.ADDED_METHOD_NAME, int.class, Object[].class);
         m.invoke(d, 10, null);
      }
   }

}
