package org.fakereplace.data;

import java.util.Map;
import java.util.Set;

import org.fakereplace.manip.util.MapFunction;

import com.google.common.collect.MapMaker;

/**
 * This class is responsible for tracking instances of certain classes as they
 * are loaded
 * 
 * @author stuart
 * 
 */
public class InstanceTracker
{

   private static Object TEMP = new Object();

   static private Map<String, Map<Object, Object>> data = new MapMaker().weakKeys().initialCapacity(100).makeComputingMap(new MapFunction(true));

   public static void add(String type, Object object)
   {
      Map<Object, Object> set = data.get(type);
      set.put(object, TEMP);
   }

   public static Set<Object> get(String type)
   {
      return data.get(type).keySet();
   }
}
