package org.fakereplace.test.replacement.virtualmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;

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

      boolean value = false, addValue = false, stuff = false;
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
         if (m.getName().equals("getStuff"))
         {
            stuff = true;
         }
      }
      assert addValue;
      assert value;
      assert stuff;
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodModifiers() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = VirtualClass.class;
      Method add = c.getMethod("addValue", int.class);
      assert !Modifier.isStatic(add.getModifiers());
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodExceptionsByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = VirtualClass.class;
      Method add = c.getMethod("addValue", int.class);
      assert add.getExceptionTypes()[0].equals(ArithmeticException.class);
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodGernericTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {

      Class<?> c = VirtualClass.class;
      Method meth = c.getMethod("getStuff", List.class);
      assert ((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()[0].equals(String.class);
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodGernericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = VirtualClass.class;
      Method meth = c.getMethod("getStuff", List.class);
      assert ((ParameterizedType) meth.getGenericParameterTypes()[0]).getActualTypeArguments()[0].equals(Integer.class);
   }

}
