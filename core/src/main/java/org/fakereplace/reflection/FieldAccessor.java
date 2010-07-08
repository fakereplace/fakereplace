package org.fakereplace.reflection;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Class that knows how to set and get replaced fields.
 * This is very inefficent, and should be replaced with
 * bytecode generation based reflection
 * 
 * @author stuart
 * 
 */
public class FieldAccessor
{

   final Class<?> clazz;
   final Integer mapKey;
   final Field map;

   public FieldAccessor(Class<?> clazz, int mapKey)
   {
      this.clazz = clazz;
      this.mapKey = mapKey;
      try
      {
         this.map = clazz.getField(org.fakereplace.boot.Constants.ADDED_FIELD_NAME);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void set(Object object, Object value)
   {
      try
      {
         Map ar = (Map) map.get(object);
         ar.put(mapKey, value);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public Object get(Object object)
   {
      try
      {
         Map ar = (Map) map.get(object);
         return ar.get(mapKey);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
