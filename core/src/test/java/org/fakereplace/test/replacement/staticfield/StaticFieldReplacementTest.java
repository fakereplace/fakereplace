package org.fakereplace.test.replacement.staticfield;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StaticFieldReplacementTest
{

   @BeforeClass
   public void setup()
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(StaticFieldClass.class, StaticFieldClass1.class);
      r.replaceQueuedClasses(true);
   }

   @Test
   public void testStaticFieldReplacement() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Method m = StaticFieldClass.class.getMethod("incAndGet");
      Long v = (Long) m.invoke(null);
      assert v.equals(new Long(1)) : "expected 1, got " + v;
      v = (Long) m.invoke(null);
      assert v.equals(new Long(2)) : "expected 2, got " + v;
      ;
   }
}
