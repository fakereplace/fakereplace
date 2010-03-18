package org.fakereplace.reflection;

import java.lang.reflect.Field;

/**
 * Class that knows how to set and get replaced fields.
 * This is very inefficent, and should be replaced with 
 * bytecode generation based reflection
 * @author stuart
 *
 */
public class FieldAccessor
{

   final Class<?> clazz;
   final int arrayItem;
   final Field array;

   public FieldAccessor(Class<?> clazz, int arrayItem)
   {
      this.clazz = clazz;
      this.arrayItem = arrayItem;
      try
      {
         this.array = clazz.getField(org.fakereplace.boot.Constants.ADDED_FIELD_NAME);
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
         Object[] ar = (Object[]) array.get(object);
         ar[arrayItem] = value;
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
         Object[] ar = (Object[]) array.get(object);
         return ar[arrayItem];
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
