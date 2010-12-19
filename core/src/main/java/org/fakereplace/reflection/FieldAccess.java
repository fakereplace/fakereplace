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
      setFakeField(f, object, val);
   }

   public static void setBoolean(Field f, Object object, boolean val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setByte(Field f, Object object, byte val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setChar(Field f, Object object, char val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setDouble(Field f, Object object, double val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setFloat(Field f, Object object, float val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setInt(Field f, Object object, int val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setLong(Field f, Object object, long val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static void setShort(Field f, Object object, short val) throws IllegalAccessException
   {
      setFakeField(f, object, val);
   }

   public static Object get(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return accessor.get(object);
   }

   public static boolean getBoolean(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Boolean) accessor.get(object);
   }

   public static byte getByte(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Byte) accessor.get(object);

   }

   public static char getChar(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Character) accessor.get(object);

   }

   public static Double getDouble(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Double) accessor.get(object);
   }

   public static float getFloat(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Float) accessor.get(object);
   }

   public static int getInt(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Integer) accessor.get(object);
   }

   public static long getLong(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Long) accessor.get(object);
   }

   public static Object getShort(Field f, Object object) throws IllegalAccessException
   {
      FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
      return (Short) accessor.get(object);
   }

   /**
    * set fake field instance field values
    * 
    * @param f
    * @return false if not a fake field
    */
   public static boolean setFakeField(Field f, Object object, Object val)
   {
      if ((f.getModifiers() & Modifier.STATIC) == 0 && f.getDeclaringClass().getName().startsWith(org.fakereplace.boot.Constants.GENERATED_CLASS_PACKAGE))
      {
         FieldAccessor accessor = ClassDataStore.getFieldAccessor(f.getDeclaringClass().getName());
         accessor.set(object, val);
         return true;
      }
      return false;
   }

   public static boolean isFakeField(Field f)
   {
      if ((f.getModifiers() & Modifier.STATIC) == 0 && f.getDeclaringClass().getName().startsWith(org.fakereplace.boot.Constants.GENERATED_CLASS_PACKAGE))
      {
         return true;
      }
      return false;
   }

}
