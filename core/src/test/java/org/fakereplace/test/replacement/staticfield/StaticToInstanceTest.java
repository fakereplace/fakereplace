package org.fakereplace.test.replacement.staticfield;

import java.lang.reflect.Field;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * when changing instance fields to static existing reference will still
 * reference the instance field
 * 
 * @author stuart
 * 
 */
public class StaticToInstanceTest
{
   @BeforeClass
   public void setup()
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(StaticToInstance.class, StatictoInstance1.class);
      r.replaceQueuedClasses();
   }

   @Test
   public void testStaticToInstance()
   {
      StaticToInstance f1 = new StaticToInstance();
      StaticToInstance f2 = new StaticToInstance();
      f1.setField(100);
      assert f2.getField() != 100;
   }

   @Test
   public void testStaticToInstanceViaReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      StaticToInstance f1 = new StaticToInstance();
      StaticToInstance f2 = new StaticToInstance();
      Field f = f1.getClass().getDeclaredField("field");
      f.setAccessible(true);
      f.setInt(f1, 200);
      assert f.getInt(f2) != 200;
   }

}
