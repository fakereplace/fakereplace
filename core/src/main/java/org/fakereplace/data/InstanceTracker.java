package org.fakereplace.data;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * This class is responsible for tracking instances of certain classes as they are loaded
 * @author stuart
 *
 */
public class InstanceTracker
{

   static private Map<String, Map<Object, Object>> data = new MapMaker().weakKeys().initialCapacity(100).makeComputingMap(new Function<String, Map<Object, Object>>()
   {

      public Map<Object, Object> apply(String from)
      {
         return new MapMaker().weakKeys().initialCapacity(100).makeMap();
      }

   });

   public static void add(String type, Object object)
   {
      Map<Object, Object> set = data.get(type);
      set.put(object, new Object());
   }

   public static Set<Object> get(String type)
   {
      return data.get(type).keySet();
   }
}
