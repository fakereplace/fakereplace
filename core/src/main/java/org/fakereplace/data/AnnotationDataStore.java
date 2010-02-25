package org.fakereplace.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.AnnotationsAttribute;

import org.fakereplace.util.AnnotationInstanceProvider;

/**
 * Stores information about the annotations on reloaded classes
 * 
 * @author stuart
 * 
 */
public class AnnotationDataStore
{

   static AnnotationInstanceProvider provider = new AnnotationInstanceProvider();

   static Map<Class<?>, Annotation[]> classAnnotations = Collections.synchronizedMap(new HashMap<Class<?>, Annotation[]>());

   static Map<Class<?>, Map<Class<? extends Annotation>, Annotation>> classAnnotationsByType = Collections.synchronizedMap(new HashMap<Class<?>, Map<Class<? extends Annotation>, Annotation>>());

   static Map<Field, Annotation[]> fieldAnnotations = Collections.synchronizedMap(new HashMap<Field, Annotation[]>());

   static Map<Field, Map<Class<? extends Annotation>, Annotation>> fieldAnnotationsByType = Collections.synchronizedMap(new HashMap<Field, Map<Class<? extends Annotation>, Annotation>>());

   static Map<Method, Annotation[]> methodAnnotations = Collections.synchronizedMap(new HashMap<Method, Annotation[]>());

   static Map<Method, Map<Class<? extends Annotation>, Annotation>> methodAnnotationsByType = Collections.synchronizedMap(new HashMap<Method, Map<Class<? extends Annotation>, Annotation>>());

   static Map<Method, Annotation[][]> parameterAnnotations = Collections.synchronizedMap(new HashMap<Method, Annotation[][]>());

   static Map<Constructor<?>, Annotation[]> constructorAnnotations = Collections.synchronizedMap(new HashMap<Constructor<?>, Annotation[]>());

   static Map<Constructor<?>, Map<Class<? extends Annotation>, Annotation>> constructorAnnotationsByType = Collections.synchronizedMap(new HashMap<Constructor<?>, Map<Class<? extends Annotation>, Annotation>>());

   static Map<Constructor<?>, Annotation[][]> constructorParameterAnnotations = Collections.synchronizedMap(new HashMap<Constructor<?>, Annotation[][]>());

   static public boolean isClassDataRecorded(Class<?> clazz)
   {
      return classAnnotations.containsKey(clazz);
   }

   static public Annotation[] getClassAnnotations(Class<?> clazz)
   {
      return classAnnotations.get(clazz);
   }

   static public Annotation getClassAnnotation(Class<?> clazz, Class<? extends Annotation> annotation)
   {
      return classAnnotationsByType.get(clazz).get(annotation);
   }

   static public boolean isClassAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotation)
   {
      return classAnnotationsByType.get(clazz).containsKey(annotation);
   }

   static public boolean isFieldDataRecorded(Field clazz)
   {
      return fieldAnnotations.containsKey(clazz);
   }

   static public Annotation[] getFieldAnnotations(Field clazz)
   {
      return fieldAnnotations.get(clazz);
   }

   static public Annotation getFieldAnnotation(Field clazz, Class<? extends Annotation> annotation)
   {
      return fieldAnnotationsByType.get(clazz).get(annotation);
   }

   static public boolean isFieldAnnotationPresent(Field clazz, Class<? extends Annotation> annotation)
   {
      return fieldAnnotationsByType.get(clazz).containsKey(annotation);
   }

   static public boolean isMethodDataRecorded(Method clazz)
   {
      return methodAnnotations.containsKey(clazz);
   }

   static public Annotation[] getMethodAnnotations(Method clazz)
   {
      return methodAnnotations.get(clazz);
   }

   static public Annotation getMethodAnnotation(Method clazz, Class<? extends Annotation> annotation)
   {
      return methodAnnotationsByType.get(clazz).get(annotation);
   }

   static public boolean isMethodAnnotationPresent(Method clazz, Class<? extends Annotation> annotation)
   {
      return methodAnnotationsByType.get(clazz).containsKey(annotation);
   }

   static public Annotation[][] getMethodParameterAnnotations(Method clazz)
   {
      return parameterAnnotations.get(clazz);
   }

   static public void recordClassAnnotations(Class<?> clazz, AnnotationsAttribute annotations)
   {
      // no annotations
      if (annotations == null)
      {
         Annotation[] ans = new Annotation[0];
         classAnnotations.put(clazz, ans);
         classAnnotationsByType.put(clazz, Collections.EMPTY_MAP);
         return;
      }
      Annotation[] ans = new Annotation[annotations.getAnnotations().length];
      classAnnotations.put(clazz, ans);
      Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
      classAnnotationsByType.put(clazz, anVals);
      int count = 0;
      for (javassist.bytecode.annotation.Annotation a : annotations.getAnnotations())
      {
         Annotation newAn = AnnotationBuilder.createAnnotation(clazz.getClassLoader(), provider, a);
         ans[count] = newAn;
         anVals.put(newAn.annotationType(), newAn);
         count++;
      }
   }

   static public void recordFieldAnnotations(Field clazz, AnnotationsAttribute annotations)
   {
      // no annotations
      if (annotations == null)
      {
         Annotation[] ans = new Annotation[0];
         fieldAnnotations.put(clazz, ans);
         fieldAnnotationsByType.put(clazz, Collections.EMPTY_MAP);
         return;
      }
      Annotation[] ans = new Annotation[annotations.getAnnotations().length];
      fieldAnnotations.put(clazz, ans);
      Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
      fieldAnnotationsByType.put(clazz, anVals);
      int count = 0;
      for (javassist.bytecode.annotation.Annotation a : annotations.getAnnotations())
      {
         Annotation newAn = AnnotationBuilder.createAnnotation(clazz.getDeclaringClass().getClassLoader(), provider, a);
         ans[count] = newAn;
         anVals.put(newAn.annotationType(), newAn);
         count++;
      }
   }

   static public void recordMethodAnnotations(Method clazz, AnnotationsAttribute annotations)
   {
      // no annotations
      if (annotations == null)
      {
         Annotation[] ans = new Annotation[0];
         methodAnnotations.put(clazz, ans);
         methodAnnotationsByType.put(clazz, Collections.EMPTY_MAP);
         return;
      }
      Annotation[] ans = new Annotation[annotations.getAnnotations().length];
      methodAnnotations.put(clazz, ans);
      Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
      methodAnnotationsByType.put(clazz, anVals);
      int count = 0;
      for (javassist.bytecode.annotation.Annotation a : annotations.getAnnotations())
      {
         Annotation newAn = AnnotationBuilder.createAnnotation(clazz.getDeclaringClass().getClassLoader(), provider, a);
         ans[count] = newAn;
         anVals.put(newAn.annotationType(), newAn);
         count++;
      }
   }

   static public void recordConstructorAnnotations(Constructor<?> clazz, AnnotationsAttribute annotations)
   {
      // no annotations
      if (annotations == null)
      {
         Annotation[] ans = new Annotation[0];
         constructorAnnotations.put(clazz, ans);
         constructorAnnotationsByType.put(clazz, Collections.EMPTY_MAP);
         return;
      }
      Annotation[] ans = new Annotation[annotations.getAnnotations().length];
      constructorAnnotations.put(clazz, ans);
      Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
      constructorAnnotationsByType.put(clazz, anVals);
      int count = 0;
      for (javassist.bytecode.annotation.Annotation a : annotations.getAnnotations())
      {
         Annotation newAn = AnnotationBuilder.createAnnotation(clazz.getDeclaringClass().getClassLoader(), provider, a);
         ans[count] = newAn;
         anVals.put(newAn.annotationType(), newAn);
         count++;
      }
   }
}
