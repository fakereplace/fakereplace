package org.fakereplace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.data.ClassData;

/**
 * This class stores all the information about classes that have been seen by
 * the transformer. Locking is achieved through the use of a synchronized map
 * 
 * @author stuart
 * 
 */
public class LoadedClassInformation
{

   /**
    * map of class information, stored by classloader -> java name (the one with
    * dots)
    */
   static Map<ClassLoader, Map<String, ClassData>> classInformation = new ConcurrentHashMap<ClassLoader, Map<String, ClassData>>();

   public static ClassData getClassInformation(Class<?> clazz)
   {
      Map<String, ClassData> data = classInformation.get(clazz.getClassLoader());
      if (data != null)
      {
         return data.get(clazz.getName());
      }
      return null;
   }

   public static void addClassInformation(ClassData data)
   {
      Map<String, ClassData> map = classInformation.get(data.getLoader());
      if (map == null)
      {
         map = new HashMap<String, ClassData>();
         classInformation.put(data.getLoader(), map);
      }
      map.put(data.getClassName(), data);
   }

}
