package org.fakereplace.manip.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * class that figures out which maniluation should be applied based on the classloader of the relative classes.
 * @author stuart
 *
 * @param <T>
 */
public class ManipulationDataStore<T extends ClassloaderFiltered<T>>
{
   private final ClassLoader NULL_CLASS_LOADER = new ClassLoader()
   {
   };

   Map<ClassLoader, Map<String, Set<T>>> cldata = new MapMaker().weakKeys().makeComputingMap(new Function<ClassLoader, Map<String, Set<T>>>()
   {
      public Map<String, Set<T>> apply(ClassLoader from)
      {
         return new MapMaker().makeMap();
      }
   });

   public Map<String, Set<T>> getManipulationData(ClassLoader loader)
   {
      if (loader == null)
      {
         loader = NULL_CLASS_LOADER;
      }
      Map<String, Set<T>> ret = new HashMap<String, Set<T>>();
      for (Entry<ClassLoader, Map<String, Set<T>>> centry : cldata.entrySet())
      {
         for (Entry<String, Set<T>> e : centry.getValue().entrySet())
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
      }

      return ret;
   }

   public void add(String name, T mdata)
   {
      ClassLoader loader = mdata.getClassLoader();
      if (loader == null)
      {
         loader = NULL_CLASS_LOADER;
      }
      Map<String, Set<T>> data = cldata.get(loader);
      if (!data.containsKey(name))
      {
         data.put(name, new CopyOnWriteArraySet<T>());
      }
      data.get(name).add(mdata);
   }

   /**
    * even though it is tempting to just try loaderOfClassBeingManipulated.loadClass(manipClassName) if this class
    * has not been loaded yet then this will cause problems, as this class will not go through the agent. Instead we have 
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
      if (classLoader == null)
      {
         classLoader = NULL_CLASS_LOADER;
      }
      Map<String, Set<T>> data = cldata.get(classLoader);
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
