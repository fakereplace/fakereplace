package org.fakereplace.integration.jsf;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.ClassChangeNotifier;
import org.fakereplace.data.InstanceTracker;

public class ClassRedefinitionPlugin implements ClassChangeAware
{
   public ClassRedefinitionPlugin()
   {
      ClassChangeNotifier.add(this);
   }

   Field getField(Class<?> clazz, String name) throws NoSuchFieldException
   {
      if (clazz == Object.class)
         throw new NoSuchFieldException();
      try
      {
         return clazz.getDeclaredField(name);
      }
      catch (Exception e)
      {
         // TODO: handle exception
      }
      return getField(clazz.getSuperclass(), name);
   }

   public void beforeChange(Class<?>[] changed, Class<?>[] added)
   {

   }

   public void notify(Class<?>[] changed, Class<?>[] added)
   {
      try
      {
         Introspector.flushCaches();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      Set<Object> data = InstanceTracker.get("javax.el.BeanELResolver");
      for (Object i : data)
      {
         clearBeanElResolver(i);
      }
   }

   public void clearBeanElResolver(Object r)
   {
      try
      {
         try
         {
            Field cacheField = getField(r.getClass(), "cache");
            cacheField.setAccessible(true);
            Object cache = cacheField.get(r);
            try
            {
               Method m = cache.getClass().getMethod("clear");
               m.invoke(cache);
            }
            catch (NoSuchMethodException e)
            {
               // different version of jboss el
               Class<?> cacheClass = getClass().getClassLoader().loadClass("javax.el.BeanELResolver$ConcurrentCache");
               Constructor<?> con = cacheClass.getConstructor(int.class);
               con.setAccessible(true);
               Object cacheInstance = con.newInstance(100);
               cacheField.set(r, cacheInstance);
            }

         }
         catch (NoSuchFieldException ee)
         {
            Field props = getField(r.getClass(), "properties");
            props.setAccessible(true);
            Object cache = props.get(r);
            Method m = cache.getClass().getMethod("clear");
            m.invoke(cache);
         }
      }
      catch (Exception e)
      {
         System.out.println("Could not clear EL cache:" + e.getMessage());
      }
   }

}
