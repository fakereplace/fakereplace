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
      rep.queueClassForReplacement(VirtualCaller.class, VirtualCaller1.class);
      rep.queueClassForReplacement(NoSupChild.class, NoSupChild1.class);
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
   public void testVirtualMethodChildByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {

      VirtualChild ns = new VirtualChild();
      Class c = VirtualChild.class;
      Method get = c.getMethod("getValue");

      Method add = c.getMethod("addValue", int.class);
      assert get != null;
      Integer res = (Integer) get.invoke(ns);
      assert res.equals(new Integer(1)) : " actual " + res;
      add.invoke(ns, 1);
      res = (Integer) get.invoke(ns);
      assert res.equals(new Integer(3)) : "Expected 3 got " + res;

   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {

      VirtualClass ns = new VirtualClass();
      VirtualCaller caller = new VirtualCaller();
      caller.add(ns);
      int val = ns.getValue();
      assert val == 11 : "expected 10 got " + val;

   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodModifiers() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = VirtualClass.class;
      Method add = c.getMethod("addValue", int.class);
      assert !Modifier.isStatic(add.getModifiers());
      add = c.getMethod("getStuff", List.class);
      assert !Modifier.isStatic(add.getModifiers());
      add = c.getMethod("getValue");
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

   @Test(groups = "virtualmethod")
   public void testVirtualMethodgetDeclaredMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      boolean add = false;
      boolean stuff = false;
      Class<?> c = VirtualClass.class;
      for (Method m : c.getDeclaredMethods())
      {
         if (m.getName().equals("addValue"))
         {
            assert m.getParameterTypes()[0] == int.class;
            add = true;
         }
         if (m.getName().equals("getStuff"))
         {
            assert m.getParameterTypes()[0] == List.class;
            stuff = true;
         }
      }
      assert add;
      assert stuff;
   }

   @Test(groups = "virtualmethod")
   public void testVirtualChildMethodgetMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      boolean add = false;
      boolean stuff = false;
      Class<?> c = VirtualChild.class;
      for (Method m : c.getMethods())
      {
         if (m.getName().equals("addValue"))
         {
            assert m.getParameterTypes()[0] == int.class;
            add = true;
         }
         if (m.getName().equals("getStuff"))
         {
            assert m.getParameterTypes()[0] == List.class;
            stuff = true;
         }
      }
      assert add;
      assert stuff;
   }

   @Test(groups = "virtualmethod")
   public void testVirtualMethodgetMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      boolean add = false;
      boolean stuff = false;
      Class<?> c = VirtualClass.class;
      for (Method m : c.getMethods())
      {
         if (m.getName().equals("addValue"))
         {
            assert m.getParameterTypes()[0] == int.class;
            add = true;
         }
         if (m.getName().equals("getStuff"))
         {
            assert m.getParameterTypes()[0] == List.class;
            stuff = true;
         }
      }
      assert add;
      assert stuff;
   }

   @Test(groups = "virtualmethod")
   public void testToStringOverride() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      VirtualClass c = new VirtualClass();
      assert getString(c).equals("VirtualChild1");
   }

   @Test(groups = "virtualmethod")
   public void testAddedDelegateMethodsDoNotBreakOnReplacement() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      VirtualCaller c = new VirtualCaller();
      assert c.toString().contains("VirtualCaller") : c.toString();
   }

   @Test(groups = "virtualmethod")
   public void testOverrideWithSuperclassNotLoaded() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      NoSupClass c = new NoSupChild();
      assert c.getStuff(0, 0, "", 0, 0).equals("NoSupChild") : "Expected NoSupChild got:" + c.getStuff(0, 0, "", 0, 0);
   }

   @Test(groups = "virtualmethod")
   public void testOverrideWithSuperclassNotLoadedSuperclassNotChanged() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      NoSupClass c = new NoSupClass();
      assert c.getStuff(0, 0, "", 0, 0).equals("NoSupClass") : "Expected NoSupClass got:" + c.getStuff(0, 0, "", 0, 0);
   }

   @Test(groups = "virtualmethod")
   public void testOverrideWithSuperclassNotLoadedOtherChildNotChanged() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      NoSupUnmodifiedChild c = new NoSupUnmodifiedChild();
      assert c.getStuff(0, 0, "", 0, 0).equals("NoSupUnmodifiedChild") : "Expected NoSupUnmodifiedChild got:" + c.getStuff(0, 0, "", 0, 0);
   }

   public String getString(Object o)
   {
      return o.toString();
   }

}
