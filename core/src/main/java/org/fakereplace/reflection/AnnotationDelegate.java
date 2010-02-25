package org.fakereplace.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fakereplace.data.AnnotationDataStore;

public class AnnotationDelegate
{
   static public boolean isAnnotationPresent(Class clazz, Class anType)
   {
      if (AnnotationDataStore.isClassDataRecorded(clazz))
      {
         boolean result = AnnotationDataStore.isClassAnnotationPresent(clazz, anType);
         // TODO: @Inherited

         return result;
      }
      return clazz.isAnnotationPresent(anType);
   }

   static public Annotation getAnnotation(Class<?> clazz, Class anType)
   {
      if (AnnotationDataStore.isClassDataRecorded(clazz))
      {
         Annotation result = AnnotationDataStore.getClassAnnotation(clazz, anType);
         // TODO: @Inherited

         return result;
      }
      return clazz.getAnnotation(anType);
   }

   static public Annotation[] getAnnotations(Class<?> clazz)
   {
      if (AnnotationDataStore.isClassDataRecorded(clazz))
      {
         Annotation[] result = AnnotationDataStore.getClassAnnotations(clazz);
         return result;
      }
      return clazz.getAnnotations();
   }

   static public boolean isAnnotationPresent(Field clazz, Class anType)
   {
      if (AnnotationDataStore.isFieldDataRecorded(clazz))
      {
         boolean result = AnnotationDataStore.isFieldAnnotationPresent(clazz, anType);
         // TODO: @Inherited

         return result;
      }
      return clazz.isAnnotationPresent(anType);
   }

   static public Annotation getAnnotation(Field clazz, Class anType)
   {
      if (AnnotationDataStore.isFieldDataRecorded(clazz))
      {
         Annotation result = AnnotationDataStore.getFieldAnnotation(clazz, anType);
         // TODO: @Inherited

         return result;
      }
      return clazz.getAnnotation(anType);
   }

   static public Annotation[] getAnnotations(Field clazz)
   {
      if (AnnotationDataStore.isFieldDataRecorded(clazz))
      {
         Annotation[] result = AnnotationDataStore.getFieldAnnotations(clazz);
         return result;
      }
      return clazz.getAnnotations();
   }

   static public boolean isAnnotationPresent(Method clazz, Class anType)
   {
      if (AnnotationDataStore.isMethodDataRecorded(clazz))
      {
         boolean result = AnnotationDataStore.isMethodAnnotationPresent(clazz, anType);
         // TODO: @Inherited

         return result;
      }
      return clazz.isAnnotationPresent(anType);
   }

   static public Annotation getAnnotation(Method clazz, Class anType)
   {
      if (AnnotationDataStore.isMethodDataRecorded(clazz))
      {
         Annotation result = AnnotationDataStore.getMethodAnnotation(clazz, anType);
         // TODO: @Inherited

         return result;
      }
      return clazz.getAnnotation(anType);
   }

   static public Annotation[] getAnnotations(Method clazz)
   {
      if (AnnotationDataStore.isMethodDataRecorded(clazz))
      {
         Annotation[] result = AnnotationDataStore.getMethodAnnotations(clazz);
         return result;
      }
      return clazz.getAnnotations();
   }

   static public Annotation[][] getParameterAnnotations(Method clazz)
   {
      if (AnnotationDataStore.isMethodDataRecorded(clazz))
      {
         Annotation[][] result = AnnotationDataStore.getMethodParameterAnnotations(clazz);
         return result;
      }
      return clazz.getParameterAnnotations();
   }

}
