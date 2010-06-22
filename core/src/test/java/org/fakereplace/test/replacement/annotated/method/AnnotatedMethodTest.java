package org.fakereplace.test.replacement.annotated.method;

import java.lang.reflect.Method;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AnnotatedMethodTest
{

   @BeforeClass
   public void setup()
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(MethodAnnotated.class, MethodAnnotated1.class);
      r.replaceQueuedClasses();
   }

   @Test
   public void testMethodAnnotationGetAnnotation() throws SecurityException, NoSuchMethodException
   {

      Method m1 = MethodAnnotated.class.getMethod("method1");
      Method m2 = MethodAnnotated.class.getMethod("method2");
      Method m3 = MethodAnnotated.class.getMethod("method3");
      Method m4 = MethodAnnotated.class.getMethod("method4");
      assert m1.getAnnotation(MethodAnnotation.class).value().equals("1");
      assert !m2.isAnnotationPresent(MethodAnnotation.class);
      assert m3.getAnnotation(MethodAnnotation.class).value().equals("3");
      assert !m4.isAnnotationPresent(MethodAnnotation.class);
   }

   @Test
   public void testMethodAnnotationGetDeclaredAnnotations() throws SecurityException, NoSuchMethodException
   {
      Method m1 = MethodAnnotated.class.getMethod("method1");
      Method m2 = MethodAnnotated.class.getMethod("method2");
      Method m3 = MethodAnnotated.class.getMethod("method3");
      Method m4 = MethodAnnotated.class.getMethod("method4");

      assert m1.getDeclaredAnnotations().length == 1 : m1.getDeclaredAnnotations().length;
      assert m1.getDeclaredAnnotations()[0].annotationType() == MethodAnnotation.class;

      assert m2.getDeclaredAnnotations().length == 0;

      assert m3.getDeclaredAnnotations().length == 1;
      assert m3.getDeclaredAnnotations()[0].annotationType() == MethodAnnotation.class;

      assert m4.getDeclaredAnnotations().length == 0;
   }

   @Test
   public void testMethodAnnotationGetAnnotations() throws SecurityException, NoSuchMethodException
   {
      Method m1 = MethodAnnotated.class.getMethod("method1");
      Method m2 = MethodAnnotated.class.getMethod("method2");
      Method m3 = MethodAnnotated.class.getMethod("method3");
      Method m4 = MethodAnnotated.class.getMethod("method4");

      assert m1.getAnnotations().length == 1 : m1.getDeclaredAnnotations().length;
      assert m1.getAnnotations()[0].annotationType() == MethodAnnotation.class;

      assert m2.getAnnotations().length == 0;

      assert m3.getAnnotations().length == 1;
      assert m3.getAnnotations()[0].annotationType() == MethodAnnotation.class;

      assert m4.getAnnotations().length == 0;
   }

}
