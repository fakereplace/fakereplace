package org.fakereplace.reflection;

import org.fakereplace.data.FieldDataStore;

/**
 * Class that knows how to set and get replaced fields. This is very inefficent,
 * and should be replaced with bytecode generation based reflection
 * 
 * @author stuart
 * 
 */
public class FieldAccessor
{

   final Class<?> clazz;
   final Integer mapKey;

   public FieldAccessor(Class<?> clazz, int mapKey)
   {
      this.clazz = clazz;
      this.mapKey = mapKey;
   }

   public void set(Object object, Object value)
   {
      FieldDataStore.setValue(object, value, mapKey);
   }

   public Object get(Object object)
   {
      return FieldDataStore.getValue(object, mapKey);
   }

}
