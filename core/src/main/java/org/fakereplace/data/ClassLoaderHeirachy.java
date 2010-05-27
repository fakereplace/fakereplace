package org.fakereplace.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that store information about ClassLoader hierarchies
 * 
 * This is uesed in order to attempt to determine if a reference 
 * to a class name in a given class file refers to an existing 
 * definition of a class or a new one. 
 * 
 * If the class loader in question is has the class loader that 
 * loaded the class as part of it's hierarchy then it is considered
 * to be the same class. 
 * 
 * This is not a 100% accurate way of doing things, as some class loaders
 * delegate to other class loaders that are not their parent. However their
 * is not much that can be done about those cases. 
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 *
 */
public class ClassLoaderHeirachy
{
   static final ConcurrentHashMap<ClassLoader, Set<ClassLoader>> accessibleClassLoaders = new ConcurrentHashMap<ClassLoader, Set<ClassLoader>>();
   
   static public void add(ClassLoader loader)
   {
      if(!accessibleClassLoaders.containsKey(loader))
      {
         Set<ClassLoader> lds = new HashSet<ClassLoader>();
         ClassLoader l = loader;
         while(l != null)
         {
            lds.add(l);
            l = l.getParent();
         }
         accessibleClassLoaders.put(loader, Collections.unmodifiableSet(lds));
      }
   }
   
   static public boolean canSeeClass(ClassLoader loader, ClassLoader loaderOfClass)
   {
      Set<ClassLoader> a = accessibleClassLoaders.get(loader);
      if(a != null)
      {
         return a.contains(loaderOfClass);
      }
      return false;
   }
}
