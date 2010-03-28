package org.fakereplace.test.replacement.constructor;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ConstructorTest
{
   @BeforeClass(groups = "constructor")
   public void setup()
   {
      ClassReplacer rep = new ClassReplacer();
      rep.queueClassForReplacement(ConstructorClass.class, ConstructorClass1.class);
      rep.queueClassForReplacement(ConstructorCallingClass.class, ConstructorCallingClass1.class);
      rep.replaceQueuedClasses();
   }

   @Test(groups = "constructor")
   public void testConstructor()
   {

      assert ConstructorCallingClass.getInstance().getValue().equals("b") : "wrong value : " + ConstructorCallingClass.getInstance().getValue();

   }

}
