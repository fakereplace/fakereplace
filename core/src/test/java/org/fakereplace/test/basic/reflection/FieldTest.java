package org.fakereplace.test.basic.reflection;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

public class FieldTest
{
   @Test()
   public void testFieldAccess() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {

      DoStuff d = new DoStuff();
      Field f = d.getClass().getField("field");
      String s = (String) f.get(d);
      assert s.equals("hello world");
      f.set(d, "bye world");
      s = (String) f.get(d);
      assert s.equals("bye world");

   }

}
