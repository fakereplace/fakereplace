package org.fakereplace.test.replacement.staticfield.repeat;

import java.lang.reflect.Field;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

public class StaticFieldRepeatReplacementTest
{
   @Test
   public void firstReplacement() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(StaticFieldRepeatClass.class, StaticFieldRepeatClass1.class);
      r.replaceQueuedClasses();

      Field someField = StaticFieldRepeatClass.class.getDeclaredField("someField");
      someField.setAccessible(true);
      someField.set(null, 10);
      Field otherField = StaticFieldRepeatClass.class.getDeclaredField("otherField");
      otherField.set(null, this);
      Field removedField = StaticFieldRepeatClass.class.getDeclaredField("removedField");
   }

   @Test(dependsOnMethods = "firstReplacement")
   public void secondReplacement() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      ClassReplacer r = new ClassReplacer();
      r.queueClassForReplacement(StaticFieldRepeatClass.class, StaticFieldRepeatClass2.class);
      r.replaceQueuedClasses();

      Field someField = StaticFieldRepeatClass.class.getDeclaredField("someField");
      someField.setAccessible(true);
      assert someField.get(null).equals(10);
      Field otherField = StaticFieldRepeatClass.class.getDeclaredField("otherField");
      otherField.setAccessible(true);
      assert otherField.get(null) == this;
      try
      {
         Field removedField = StaticFieldRepeatClass.class.getDeclaredField("removedField");
         assert false : "Field should have been removed";
      }
      catch (NoSuchFieldException e)
      {

      }
   }

}
