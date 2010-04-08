package org.fakereplace.test.replacement.constructor.other;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Creator
{

   public void doStuff() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      Constructor<?> c = Created.class.getDeclaredConstructor();
      c.newInstance();
   }

}
