package org.fakereplace.data;

import java.util.HashMap;
import java.util.Map;

public class ClassDataStore
{

   public static Map<ClassLoader, Map<String, ClassData>> classData = new HashMap<ClassLoader, Map<String, ClassData>>();

   public static void saveClassData(ClassLoader loader, String className, ClassData data)
   {
      if (!classData.containsKey(loader))
      {
         classData.put(loader, new HashMap<String, ClassData>());
      }
      Map<String, ClassData> map = classData.get(loader);
      map.put(className, data);
   }

   public static ClassData getClassData(ClassLoader loader, String className)
   {
      if (!classData.containsKey(loader))
      {
         return null;
      }
      Map<String, ClassData> map = classData.get(loader);
      ClassData cd = map.get(className);

      return cd;
   }

}
