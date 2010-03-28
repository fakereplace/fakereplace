package org.fakereplace.test.replacement.annotated.method;

import java.lang.reflect.Method;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

public class AnnotatedMethodTest
{

   @Test
   public void testMethodAnnotations() throws SecurityException, NoSuchMethodException
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(MethodAnnotated.class, MethodAnnotated1.class);
      r.replaceQueuedClasses();

      Method m1 = MethodAnnotated.class.getMethod("method1");
      Method m2 = MethodAnnotated.class.getMethod("method2");
      Method m3 = MethodAnnotated.class.getMethod("method3");
      assert m1.getAnnotation(MethodAnnotation.class).value().equals("1");
      assert !m2.isAnnotationPresent(MethodAnnotation.class);
      assert m3.getAnnotation(MethodAnnotation.class).value().equals("3");

   }

   @Test
   public void testMethodParameterAnnotations() throws SecurityException, NoSuchMethodException
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(MethodParameterAnnotated.class, MethodParameterAnnotated1.class);
      r.replaceQueuedClasses();

      Method m1 = MethodParameterAnnotated.class.getMethod("method1", int.class);
      Method m2 = MethodParameterAnnotated.class.getMethod("method2", int.class);
      Method m3 = MethodParameterAnnotated.class.getMethod("method3", int.class);

      assert ((MethodAnnotation) m1.getParameterAnnotations()[0][0]).value().equals("1");
      assert m2.getParameterAnnotations()[0].length == 0;
      assert ((MethodAnnotation) m3.getParameterAnnotations()[0][0]).value().equals("3");

   }

}
