package org.fakereplace.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.Descriptor;

import org.fakereplace.reflection.FieldAccessor;

import com.google.common.collect.MapMaker;

public class ClassDataStore
{

   static Map<String, Class<?>> proxyNameToReplacedClass = new ConcurrentHashMap<String, Class<?>>();
   static Map<String, FieldAccessor> proxyNameToFieldAccessor = new ConcurrentHashMap<String, FieldAccessor>();

   static Map<ClassLoader, Map<String, ClassData>> classData = new MapMaker().weakKeys().makeMap();

   static Map<ClassLoader, Map<String, BaseClassData>> baseClassData = new MapMaker().weakKeys().makeMap();
   static Map<String, MethodData> proxyNameToMethodData = new ConcurrentHashMap<String, MethodData>();

   /**
    * takes the place of the null key on ConcurrentHashMap
    */
   static ClassLoader nullLoader = new ClassLoader()
   {
   };

   public static void saveClassData(ClassLoader loader, String className, ClassDataBuilder data)
   {
      className = Descriptor.toJvmName(className);
      if (loader == null)
      {
         loader = nullLoader;
      }
      if (!classData.containsKey(loader))
      {
         classData.put(loader, new HashMap<String, ClassData>());
      }
      Map<String, ClassData> map = classData.get(loader);
      map.put(className, data.buildClassData());
   }

   public static void saveClassData(ClassLoader loader, String className, BaseClassData data)
   {
      className = className.replace('.', '/');
      if (loader == null)
      {
         loader = nullLoader;
      }
      if (!baseClassData.containsKey(loader))
      {
         baseClassData.put(loader, new HashMap<String, BaseClassData>());
      }
      Map<String, BaseClassData> map = baseClassData.get(loader);
      map.put(className, data);
   }

   public static ClassData getModifiedClassData(ClassLoader loader, String className)
   {
      className = className.replace('.', '/');
      if (loader == null)
      {
         loader = nullLoader;
      }
      if (!classData.containsKey(loader))
      {
         BaseClassData dd = getBaseClassData(loader, className);
         if (dd == null)
         {
            return null;
         }
         ClassDataBuilder builder = new ClassDataBuilder(dd);
         return builder.buildClassData();
      }
      Map<String, ClassData> map = classData.get(loader);
      ClassData cd = map.get(className);
      if (cd == null)
      {
         BaseClassData dd = getBaseClassData(loader, className);
         if (dd == null)
         {
            return null;
         }
         ClassDataBuilder builder = new ClassDataBuilder(dd);
         return builder.buildClassData();
      }

      return cd;
   }

   public static BaseClassData getBaseClassData(ClassLoader loader, String className)
   {
      className = className.replace('.', '/');
      if (loader == null)
      {
         loader = nullLoader;
      }
      if (!baseClassData.containsKey(loader))
      {
         return null;
      }
      Map<String, BaseClassData> map = baseClassData.get(loader);
      BaseClassData cd = map.get(className);
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

   public static void registerReplacedMethod(String proxyName, MethodData methodData)
   {
      proxyNameToMethodData.put(proxyName, methodData);
   }

   public static MethodData getMethodInformation(String proxyName)
   {
      return proxyNameToMethodData.get(proxyName);
   }

   public static FieldAccessor getFieldAccessor(String proxyName)
   {
      return proxyNameToFieldAccessor.get(proxyName);
   }

}
