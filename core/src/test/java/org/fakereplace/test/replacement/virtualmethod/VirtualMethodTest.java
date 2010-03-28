package org.fakereplace.test.replacement.virtualmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class VirtualMethodTest
{
   @BeforeClass(groups = "virtualmethod")
   public void setup()
   {
      ClassReplacer rep = new ClassReplacer();
      rep.queueClassForReplacement(VirtualClass.class, VirtualClass1.class);
      rep.replaceQueuedClasses();
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {

      VirtualClass ns = new VirtualClass();
      Class c = VirtualClass.class;
      Method get = c.getMethod("getValue");

      Method add = c.getMethod("addValue", int.class);
      assert get != null;
      Integer res = (Integer) get.invoke(ns);
      assert res.equals(new Integer(1));
      add.invoke(ns, 1);
      res = (Integer) get.invoke(ns);
      assert res.equals(new Integer(3)) : "Expected 3 got " + res;

      boolean value = false, addValue = false;
      for (Method m : c.getDeclaredMethods())
      {
         if (m.getName().equals("getValue"))
         {
            value = true;
         }
         if (m.getName().equals("addValue"))
         {
            addValue = true;
         }
      }
      assert addValue;
      assert value;
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodModifiers() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {

      VirtualClass ns = new VirtualClass();
      Class c = VirtualClass.class;
      Method add = c.getMethod("addValue", int.class);
      assert !Modifier.isStatic(add.getModifiers());
   }

}
