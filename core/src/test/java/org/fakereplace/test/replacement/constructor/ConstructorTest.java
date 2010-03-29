package org.fakereplace.test.replacement.constructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

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

   @Test(groups = "virtualmethod")
   public void testVirtualConstrcutorGernericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = ConstructorClass.class;
      Constructor<?> con = c.getConstructor(List.class);
      assert ((ParameterizedType) con.getGenericParameterTypes()[0]).getActualTypeArguments()[0].equals(String.class);
   }

}
