package org.fakereplace.test.replacement.finalmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FinalMethodReplacementTest
{

   @BeforeClass
   public void setup()
   {
      ClassReplacer cr = new ClassReplacer();
      cr.queueClassForReplacement(FinalMethodClass.class, FinalMethodClass1.class);
      cr.replaceQueuedClasses();
   }

   @Test
   public void testNonFinalMethodIsNonFinal() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      FinalMethodClass cl = new FinalMethodClass();
      Method method = cl.getClass().getMethod("finalMethod-replaced");
      assert Modifier.isFinal(method.getModifiers());
      assert method.invoke(cl).equals("finalMethod-replaced");
   }

   @Test
   public void testFinalMethodIsFinal() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      FinalMethodClass cl = new FinalMethodClass();
      Method method = cl.getClass().getMethod("nonFinalMethod-replaced");
      assert !Modifier.isFinal(method.getModifiers());
      assert method.invoke(cl).equals("nonFinalMethod-replaced");
   }
}
