package org.fakereplace.test.replacement.instancefield;

import java.lang.reflect.Field;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

public class InstanceFieldReflectionTest
{

   @Test
   public void testSettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      ClassReplacer c = new ClassReplacer();
      c.queueClassForReplacement(InstanceFieldReflection.class, InstanceFieldReflection1.class);
      c.replaceQueuedClasses();

      InstanceFieldReflection r = new InstanceFieldReflection();
      Field field = InstanceFieldReflection.class.getDeclaredField("value");
      field.set(r, "hello world");
      assert r.getValue().equals("hello world");

   }

}
