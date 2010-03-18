package org.fakereplace.test.replacement.instancefield;

import java.lang.reflect.Field;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InstanceFieldReflectionTest
{

   @BeforeClass(groups = "instanceFieldByReflection")
   public void setup()
   {
      ClassReplacer c = new ClassReplacer();
      c.queueClassForReplacement(InstanceFieldReflection.class, InstanceFieldReflection1.class);
      c.replaceQueuedClasses();
   }

   @Test(groups = "instanceFieldByReflection")
   public void testSettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      InstanceFieldReflection r = new InstanceFieldReflection();
      Field field = InstanceFieldReflection.class.getDeclaredField("value");
      field.set(r, "hello world");
      assert r.getValue().equals("hello world");
   }

   @Test(groups = "instanceFieldByReflection")
   public void testGettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      InstanceFieldReflection r = new InstanceFieldReflection();
      Field field = InstanceFieldReflection.class.getDeclaredField("value");
      field.get(r);
      assert r.getValue().equals("hi");
   }

}
