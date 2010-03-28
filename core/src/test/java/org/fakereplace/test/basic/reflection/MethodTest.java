package org.fakereplace.test.basic.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fakereplace.boot.Constants;
import org.testng.annotations.Test;

public class MethodTest
{
   @Test
   public void testDelegatorMethodAdded() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException
   {
      DoStuff d = new DoStuff();
      Method m = d.getClass().getMethod(Constants.ADDED_METHOD_NAME, int.class, Object[].class);
      m.invoke(d, 10, null);
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

}
