package org.fakereplace.runtime;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.MethodIdentifierStore;
import org.fakereplace.manip.util.MapFunction;

import com.google.common.collect.MapMaker;

public class VirtualDelegator
{

   static Map<String, Map<String, Set<String>>> delegatingMethods = new MapMaker().makeComputingMap(new MapFunction<String, String, Set<String>>(false));

   public static boolean contains(Object val, String callingClassName, String methodName, String methodDesc)
   {
      Class<?> c = val.getClass();
      while (!c.getName().equals(callingClassName))
      {
         if (delegatingMethods.containsKey(c.getName()))
         {
            return true;
         }
         c = c.getSuperclass();
      }
      return false;
   }

   public static Object run(Object val, String methodName, String methodDesc, Object[] params)
   {
      try
      {
         Method meth = val.getClass().getMethod(Constants.ADDED_METHOD_NAME, int.class, Object[].class);
         int methodIdentifier = MethodIdentifierStore.getMethodNumber(methodName, methodDesc);
         return meth.invoke(val, methodIdentifier, params);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
