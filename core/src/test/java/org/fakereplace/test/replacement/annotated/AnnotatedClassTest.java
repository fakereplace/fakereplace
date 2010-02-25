package org.fakereplace.test.replacement.annotated;

import java.lang.reflect.InvocationTargetException;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AnnotatedClassTest
{
   @BeforeClass(groups = "annotatedclass")
   public void setup()
   {
      ClassReplacer rep = new ClassReplacer();
      rep.queueClassForReplacement(AnnotatedClass.class, AnnotatedClass1.class);
      rep.replaceQueuedClasses();
   }

   @Test(groups = "annotatedclass")
   public void testAnnotationAccessByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      AnnotatedClass ns = new AnnotatedClass();
      Class c = AnnotatedClass.class;
      assert ns.getClass().isAnnotationPresent(Annotation2.class);
      Annotation2 an2 = ns.getClass().getAnnotation(Annotation2.class);
      assert an2.ivalue() == 10;
      assert !ns.getClass().isAnnotationPresent(Annotation1.class);
   }

}
