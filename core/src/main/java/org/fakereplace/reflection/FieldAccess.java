package org.fakereplace.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.fakereplace.data.ClassDataStore;

/**
 * Class that handles access to re-written fields.
 * 
 * @author stuart
 *
 */
public class FieldAccess
{

   public static void set(Field f, Object object, Object val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.set(object, val);
      }
   }

   public static void setBoolean(Field f, Object object, boolean val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setBoolean(object, val);
      }
   }

   public static void setByte(Field f, Object object, byte val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setByte(object, val);
      }
   }

   public static void setChar(Field f, Object object, char val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setChar(object, val);
      }
   }

   public static void setDouble(Field f, Object object, double val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setDouble(object, val);
      }
   }

   public static void setFloat(Field f, Object object, float val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setFloat(object, val);
      }
   }

   public static void setInt(Field f, Object object, int val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setInt(object, val);
      }
   }

   public static void setLong(Field f, Object object, long val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setLong(object, val);
      }
   }

   public static void setShort(Field f, Object object, short val) throws IllegalAccessException
   {
      if (!setFakeField(f, object, val))
      {
         f.setShort(object, val);
      }
   }

   /**
    * set fake field instance field values 
    * @param f
    * @return false if not a fake field
    */
   static boolean setFakeField(Field f, Object object, Object val)
   {
      if ((f.getModifiers() & Modifier.STATIC) == 0 && f.getDeclaringClass().getName().startsWith(org.fakereplace.boot.Constants.GENERATED_CLASS_PACKAGE))
      {
         FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
         accessor.set(object, val);
         return true;
      }
      return false;
   }

}
