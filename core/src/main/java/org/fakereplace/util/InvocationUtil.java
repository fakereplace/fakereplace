package org.fakereplace.util;

import java.lang.reflect.Method;

import org.fakereplace.boot.Constants;

/**
 * class that inserts an object into the front of an array
 * this is way to hard to code by hand in bytecode.
 * @author stuart
 *
 */
public class InvocationUtil
{
   static public Object[] prepare(Object object, Object[] array)
   {
      int length = 0;
      if (array != null)
      {
         length = array.length;
      }
      Object[] ret = new Object[length + 1];
      ret[0] = object;
      for (int i = 0; i < length; ++i)
      {
         ret[i + 1] = array[i];
      }
      return ret;
   }

   static public Class<?>[] getArguments(Class<?> object, Class<?>[] array)
   {
      Class<?>[] ret = new Class[array.length + 1];
      ret[0] = object;
      for (int i = 0; i < array.length; ++i)
      {
         ret[i + 1] = array[i];
      }
      return ret;
   }

   public static boolean executeFakeCall(Method method, Object argment)
   {
      if (argment != null)
      {
         if (method.getDeclaringClass().getName().startsWith(Constants.GENERATED_CLASS_PACKAGE))
         {
            return true;
         }
      }
      return false;
   }

}
