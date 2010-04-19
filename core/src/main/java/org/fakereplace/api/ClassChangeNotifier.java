package org.fakereplace.api;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class ClassChangeNotifier
{
   static WeakHashMap<ClassLoader, Set<ClassChangeAware>> classChangeAwares = new WeakHashMap<ClassLoader, Set<ClassChangeAware>>();

   /**
    * These are objects that want to be notified but that do not have a dependency on fakereplace.
    */
   static WeakHashMap<ClassLoader, Set<Object>> unlinkedAwares = new WeakHashMap<ClassLoader, Set<Object>>();

   static public void add(ClassChangeAware aware)
   {
      if (!classChangeAwares.containsKey(aware.getClass().getClassLoader()))
      {
         classChangeAwares.put(aware.getClass().getClassLoader(), new HashSet<ClassChangeAware>());
      }
      classChangeAwares.get(aware.getClass().getClassLoader()).add(aware);
   }

   static public void add(Object aware) throws SecurityException, NoSuchMethodException
   {

      if (!unlinkedAwares.containsKey(aware.getClass().getClassLoader()))
      {
         unlinkedAwares.put(aware.getClass().getClassLoader(), new HashSet<Object>());
      }
      unlinkedAwares.get(aware.getClass().getClassLoader()).add(aware);
   }

   public static void notify(Class<?>[] changed, Class<?>[] newClasses)
   {
      Class<?>[] a = new Class[0];
      for (Set<ClassChangeAware> c : classChangeAwares.values())
      {
         for (ClassChangeAware i : c)
         {
            i.notify(changed, newClasses);
         }
      }

      for (Set<Object> c : unlinkedAwares.values())
      {
         for (Object i : c)
         {
            try
            {
               Method m = i.getClass().getMethod("notify", a.getClass(), a.getClass());
               m.invoke(i, changed, newClasses);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }

}
