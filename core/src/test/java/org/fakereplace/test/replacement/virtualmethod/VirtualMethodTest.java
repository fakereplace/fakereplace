package org.fakereplace.test.replacement.virtualmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
      assert res.equals(new Integer(0));
      add.invoke(null, ns, 1);
      res = (Integer) get.invoke(ns);
      assert res.equals(new Integer(1)) : "Expected 1 got " + res;
   }

}
