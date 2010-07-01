package org.fakereplace.test.replacement.staticmethod.visibility;

import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass;
import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass1;
import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.UnchangedStaticMethodCallingClass;
import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IncreaseVisibilityTest
{
   @BeforeClass
   public void setup()
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(StaticMethodVisibilityCallingClass.class, StaticMethodVisibilityCallingClass1.class);
      r.queueClassForReplacement(StaticMethodVisibilityClass.class, StaticMethodVisibilityClass1.class);
      r.replaceQueuedClasses();
   }

   @Test
   public void testExistingMethod()
   {
      assert StaticMethodVisibilityClass.callingMethod().equals("helo world");
   }

   @Test
   public void testNewExternalMethod()
   {
      assert StaticMethodVisibilityCallingClass.callingClass().equals("helo world");
   }

   @Test
   public void testUnchangedClassCallingExternalMethod()
   {
      assert UnchangedStaticMethodCallingClass.callingClass().equals("helo world");
   }
}
