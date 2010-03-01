package org.fakereplace.test.replacement.instancefield;

import java.lang.reflect.InvocationTargetException;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InstanceFieldTest
{
   @BeforeClass(groups = "instancefield")
   public void setup()
   {
      ClassReplacer rep = new ClassReplacer();
      rep.queueClassForReplacement(InstanceFieldClass.class, InstanceFieldClass1.class);
      rep.replaceQueuedClasses();
   }

   @Test(groups = "instancefield")
   public void testAddingInstanceField() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {

      InstanceFieldClass ns = new InstanceFieldClass();
      ns.inc();
      assert ns.get() == 1;
   }

}
