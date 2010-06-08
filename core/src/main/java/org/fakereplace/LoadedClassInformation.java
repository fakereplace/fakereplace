package org.fakereplace;

import java.util.Map;

import org.fakereplace.data.ClassData;
import org.fakereplace.manip.util.MapFunction;

import com.google.common.collect.MapMaker;

/**
 * This class stores all the information about classes that have been seen by
 * the transformer. 
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
   static Map<ClassLoader, Map<String, ClassData>> classInformation = new MapMaker().weakKeys().makeComputingMap(new MapFunction(false));

   public static ClassData getClassInformation(Class<?> clazz)
   {
      Map<String, ClassData> data = classInformation.get(clazz.getClassLoader());
      return data.get(clazz.getName());
   }

   public static void addClassInformation(ClassData data)
   {
      Map<String, ClassData> map = classInformation.get(data.getLoader());
      map.put(data.getClassName(), data);
   }

}
