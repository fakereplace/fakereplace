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

}
