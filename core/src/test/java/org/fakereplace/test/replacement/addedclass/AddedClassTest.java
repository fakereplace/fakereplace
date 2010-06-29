package org.fakereplace.test.replacement.addedclass;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

public class AddedClassTest
{

   @Test
   public void testAddedClass()
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(ReplacedClass.class, ReplacedClass1.class);
      r.addNewClass(AddedClass1.class, "org.fakereplace.test.replacement.addedclass.AddedClass");
      r.replaceQueuedClasses();

      ReplacedClass c = new ReplacedClass();
      assert c.getValue().equals("hello Bob");

   }
}
