package org.fakereplace.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.reflection.FieldAccessor;

public class ClassDataStore
{

   static Map<String, Class<?>> proxyNameToReplacedClass = new ConcurrentHashMap<String, Class<?>>();
   static Map<String, FieldAccessor> proxyNameToFieldAccessor = new ConcurrentHashMap<String, FieldAccessor>();
   public static Map<ClassLoader, Map<String, ClassData>> classData = new ConcurrentHashMap<ClassLoader, Map<String, ClassData>>();

   /**
    * takes the place of the null key on ConcurrentHashMap
    */
   static ClassLoader nullLoader = new ClassLoader()
   {
   };

   public static void saveClassData(ClassLoader loader, String className, ClassData data)
   {
      if (loader == null)
      {
         loader = nullLoader;
      }
      if (!classData.containsKey(loader))
      {
         classData.put(loader, new HashMap<String, ClassData>());
      }
      Map<String, ClassData> map = classData.get(loader);
      map.put(className, data);
   }

   public static ClassData getClassData(ClassLoader loader, String className)
   {
      if (loader == null)
      {
         loader = nullLoader;
      }
      if (!classData.containsKey(loader))
      {
         return null;
      }
      Map<String, ClassData> map = classData.get(loader);
      ClassData cd = map.get(className);

      return cd;
   }

   public static Class<?> getRealClassFromProxyName(String proxyName)
   {
      return proxyNameToReplacedClass.get(proxyName);
   }

   public static void registerProxyName(Class<?> c, String proxyName)
   {
      proxyNameToReplacedClass.put(proxyName, c);
   }

   public static void registerFieldAccessor(String proxyName, FieldAccessor accessor)
   {
      proxyNameToFieldAccessor.put(proxyName, accessor);
   }

   public static FieldAccessor getFieldAccessor(String proxyName)
   {
      return proxyNameToFieldAccessor.get(proxyName);
   }

}
