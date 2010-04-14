package org.fakereplace.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class holds static data that is used by the transformation agents
 * 
 * @author stuart
 * 
 */
public class GlobalClassDefinitionData
{

   static protected ReentrantLock lock = new ReentrantLock();

   static Map<ClassLoader, Map<String, byte[]>> proxyDefinitions = new ConcurrentHashMap<ClassLoader, Map<String, byte[]>>();

   public static byte[] getProxyDefinition(ClassLoader classLoader, String name)
   {

      try
      {
         lock.lock();
         Map<String, byte[]> def = proxyDefinitions.get(classLoader);
         if (def != null)
         {
            return def.get(name);
         }
         return null;
      }
      finally
      {
         lock.unlock();
      }
   }

   public static void saveProxyDefinition(ClassLoader classLoader, String className, byte[] data)
   {
      try
      {
         lock.lock();
         Map<String, byte[]> def = proxyDefinitions.get(classLoader);
         if (def == null)
         {
            def = new HashMap<String, byte[]>();
            proxyDefinitions.put(classLoader, def);
         }
         def.put(className, data);
      }
      finally
      {
         lock.unlock();
      }
   }

   static AtomicLong proxyNo = new AtomicLong();

   /**
    * Returns a unique proxy name
    * 
    * @return
    */
   public static String getProxyName()
   {
      return Constants.GENERATED_CLASS_PACKAGE + ".ProxyClass" + proxyNo.incrementAndGet();
   }

}
