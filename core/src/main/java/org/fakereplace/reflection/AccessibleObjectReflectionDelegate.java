package org.fakereplace.reflection;

import java.lang.reflect.AccessibleObject;
import java.util.Map;

import org.fakereplace.com.google.common.collect.MapMaker;
import sun.reflect.Reflection;

import org.fakereplace.com.google.common.collect.MapMaker;

/**
 * tracks the accessible state of reflection items
 * @author stuart
 *
 */
public class AccessibleObjectReflectionDelegate
{
   static Map<AccessibleObject, Boolean> accessibleMap = new MapMaker().weakKeys().makeMap();

   public static void setAccessible(AccessibleObject object, boolean accessible)
   {
      accessibleMap.put(object, accessible);
   }

   public static boolean isAccessible(AccessibleObject object)
   {
      Boolean res = accessibleMap.get(object);
      if (res == null)
      {
         return false;
      }
      return res;
   }

   /**
    * makes sure that a caller has permission to access an AccessibleObject and
    * calls setAccessible
    * @param object
    * @param callerStackDepth
    */
   public static void ensureAccess(AccessibleObject object, int callerStackDepth, Class<?> declaringClass, int modifiers) throws IllegalAccessException
   {
      if (!isAccessible(object))
      {
         Class<?> caller = sun.reflect.Reflection.getCallerClass(callerStackDepth);
         Reflection.ensureMemberAccess(caller, declaringClass, object, modifiers);
      }
      object.setAccessible(true);
   }
}
