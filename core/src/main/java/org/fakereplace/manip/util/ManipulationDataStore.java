package org.fakereplace.manip.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * class that figures out which maniluation should be applied based on the classloader of the relative classes.
 * @author stuart
 *
 * @param <T>
 */
public class ManipulationDataStore<T extends ClassloaderFiltered<T>>
{
   Map<String, Set<T>> data = new ConcurrentHashMap<String, Set<T>>();

   public Map<String, Set<T>> getManipulationDate(ClassLoader loader)
   {
      Map<String, Set<T>> ret = new HashMap<String, Set<T>>();
      for (Entry<String, Set<T>> e : data.entrySet())
      {
         Set<T> set = new HashSet<T>();
         ret.put(e.getKey(), set);
         for (ClassloaderFiltered<T> f : e.getValue())
         {
            if (includeClassLoader(loader, f.getClassLoader()))
            {
               set.add(f.getInstane());
            }
         }
      }

      return ret;
   }

   public void add(String name, T mdata)
   {
      if (!data.containsKey(name))
      {
         data.put(name, new CopyOnWriteArraySet<T>());
      }
      data.get(name).add(mdata);
   }

   /**
    * even though it is tempting to just try loaderOfClassBeingManipulated.loadClass(manipClassName) if this class
    * has not been loaded yet then this will cause problems, as this class will not be manipulated. Instead we have 
    * to try searching through the parent classloaders, which will not always work.
    * 
    *  
    * @param loaderOfClassBeingManipulated
    * @param loaderOfManipulatedClass
    * @return
    */
   public static boolean includeClassLoader(ClassLoader loaderOfClassBeingManipulated, ClassLoader loaderOfManipulatedClass)
   {
      if (loaderOfManipulatedClass == null)
      {
         return true;
      }
      ClassLoader loader = loaderOfClassBeingManipulated;
      while (loader != null)
      {
         if (loader == loaderOfManipulatedClass)
         {
            return true;
         }
         loader = loader.getParent();
      }
      return false;
   }

   public void remove(String className, ClassLoader classLoader)
   {
      if (data.containsKey(className))
      {
         Set<T> set = data.get(className);
         Iterator<T> i = set.iterator();
         while (i.hasNext())
         {
            T val = i.next();
            if (val.getClassLoader() == classLoader)
            {
               i.remove();
            }
         }
      }
   }
}
