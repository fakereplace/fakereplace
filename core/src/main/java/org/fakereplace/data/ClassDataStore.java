package org.fakereplace.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.BuiltinClassData;
import org.fakereplace.reflection.FieldAccessor;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class ClassDataStore
{

   static Map<String, Class<?>> proxyNameToReplacedClass = new ConcurrentHashMap<String, Class<?>>();
   static Map<String, FieldAccessor> proxyNameToFieldAccessor = new ConcurrentHashMap<String, FieldAccessor>();

   static Map<ClassLoader, Map<String, ClassData>> classData = new MapMaker().weakKeys().makeComputingMap(new Function<ClassLoader, Map<String, ClassData>>()
   {

      public Map<String, ClassData> apply(ClassLoader from)
      {
         return new MapMaker().makeMap();
      }
   });

   static Map<ClassLoader, Map<String, BaseClassData>> baseClassData = new MapMaker().weakKeys().makeComputingMap(new Function<ClassLoader, Map<String, BaseClassData>>()
   {

      public Map<String, BaseClassData> apply(ClassLoader from)
      {
         return new MapMaker().makeMap();
      }
   });

   static Map<String, MethodData> proxyNameToMethodData = new ConcurrentHashMap<String, MethodData>();

   /**
    * takes the place of the null key on ConcurrentHashMap
    */
   static ClassLoader nullLoader = new ClassLoader()
   {
   };

   public static void saveClassData(ClassLoader loader, String className, ClassDataBuilder data)
   {
      className = className.replace('/', '.');
      if (loader == null)
      {
         loader = nullLoader;
      }
      Map<String, ClassData> map = classData.get(loader);
      map.put(className, data.buildClassData());
   }

   public static void saveClassData(ClassLoader loader, String className, BaseClassData data)
   {
      className = className.replace('/', '.');
      if (loader == null)
      {
         loader = nullLoader;
      }
      Map<String, BaseClassData> map = baseClassData.get(loader);
      map.put(className, data);
   }

   public static ClassData getModifiedClassData(ClassLoader loader, String className)
   {
      className = className.replace('/', '.');
      if (loader == null)
      {
         loader = nullLoader;
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
         ClassData d = builder.buildClassData();
         map.put(className, d);
         return d;
      }

      return cd;
   }

   public static BaseClassData getBaseClassData(ClassLoader loader, String className)
   {
      className = className.replace('/', '.');
      if (loader == null)
      {
         loader = nullLoader;
      }
      Map<String, BaseClassData> map = baseClassData.get(loader);
      if (!map.containsKey(className))
      {
         // if this is a class that is not being instrumented it is safe to
         // load the class and get the data
         if (BuiltinClassData.skipInstrumentation(className))
         {
            try
            {
               if (loader != nullLoader)
               {
                  Class<?> cls = loader.loadClass(className);
                  saveClassData(loader, className, new BaseClassData(cls));
               }
               else
               {
                  Class<?> cls = Class.forName(className);
                  saveClassData(loader, className, new BaseClassData(cls));
               }
            }
            catch (ClassNotFoundException e)
            {
               return null;
            }
         }
         else
         {
            return null;
         }
      }

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
