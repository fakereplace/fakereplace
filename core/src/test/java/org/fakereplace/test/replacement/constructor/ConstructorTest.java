package org.fakereplace.test.replacement.constructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.fakereplace.test.replacement.constructor.other.Creator;
import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ConstructorTest
{
   @BeforeClass(groups = "constructor")
   public void setup()
   {
      ClassReplacer rep = new ClassReplacer();
      rep.queueClassForReplacement(ConstructorClass.class, ConstructorClass1.class);
      rep.queueClassForReplacement(ConstructorCallingClass.class, ConstructorCallingClass1.class);
      rep.replaceQueuedClasses();
   }

   @Test(groups = "constructor")
   public void testConstructor()
   {
      assert ConstructorCallingClass.getInstance().getValue().equals("b") : "wrong value : " + ConstructorCallingClass.getInstance().getValue();
   }

   @Test(groups = "constructor")
   public void testConstructorByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      boolean c1 = false, c2 = false;
      for (Constructor<?> c : ConstructorClass.class.getDeclaredConstructors())
      {
         if (c.getParameterTypes().length == 1)
         {
            if (c.getParameterTypes()[0] == List.class)
            {
               c1 = true;
            }
            else if (c.getParameterTypes()[0] == String.class)
            {
               c2 = true;
            }
         }

      }
      assert c1;
      assert c2;
      Class<?> c = ConstructorClass.class;
      Constructor<?> con = c.getConstructor(List.class);
      ConstructorClass inst = (ConstructorClass) con.newInstance(null, null);
      assert inst.getValue().equals("h");
   }

   @Test(groups = "constructor")
   public void testVirtualConstrcutorGenericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = ConstructorClass.class;
      Constructor<?> con = c.getConstructor(List.class);
      assert ((ParameterizedType) con.getGenericParameterTypes()[0]).getActualTypeArguments()[0].equals(String.class);
   }

   @Test(groups = "constructor")
   public void testPackagePrivateConstructor() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      Creator c = new Creator();
      c.doStuff();
   }
}
