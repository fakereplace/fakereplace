package org.fakereplace.test.replacement.constructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.fakereplace.test.coverage.ChangeTestType;
import org.fakereplace.test.coverage.CodeChangeType;
import org.fakereplace.test.coverage.Coverage;
import org.fakereplace.test.coverage.MultipleCoverage;
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

   @Coverage(privateMember = false, change = CodeChangeType.ADD_CONSTRUCTOR, test = ChangeTestType.ACCESS_THROUGH_BYTECODE)
   @Test(groups = "constructor")
   public void testConstructor()
   {
      assert ConstructorCallingClass.getInstance().getValue().equals("b") : "wrong value : " + ConstructorCallingClass.getInstance().getValue();
   }

   @MultipleCoverage( {
         @Coverage(privateMember = true, change = CodeChangeType.ADD_CONSTRUCTOR, test = ChangeTestType.GET_DECLARED_ALL),
         @Coverage(privateMember = false, change = CodeChangeType.ADD_CONSTRUCTOR, test = ChangeTestType.GET_DECLARED_ALL) })
   @Test(groups = "constructor")
   public void testGetDeclaredConstructors() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
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
   }

   @MultipleCoverage( {
         @Coverage(privateMember = true, change = CodeChangeType.ADD_CONSTRUCTOR, test = ChangeTestType.GET_ALL),
         @Coverage(privateMember = false, change = CodeChangeType.ADD_CONSTRUCTOR, test = ChangeTestType.GET_ALL) })
   @Test(groups = "constructor")
   public void testGetConstructors() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      boolean c1 = false, c2 = false;
      for (Constructor<?> c : ConstructorClass.class.getConstructors())
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
      assert !c1;
      assert c2;
   }

   @Test(groups = "constructor")
   public void testConstructorByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
   {

      Class<?> c = ConstructorClass.class;
      Constructor<?> con = c.getDeclaredConstructor(List.class);
      ConstructorClass inst = (ConstructorClass) con.newInstance(null, null);
      assert inst.getValue().equals("h");
   }

   @Test(groups = "constructor")
   public void testVirtualConstrcutorGenericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      Class<?> c = ConstructorClass.class;
      Constructor<?> con = c.getDeclaredConstructor(List.class);
      assert ((ParameterizedType) con.getGenericParameterTypes()[0]).getActualTypeArguments()[0].equals(String.class);
   }

   @Test(groups = "constructor")
   public void testPackagePrivateConstructor() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      Creator c = new Creator();
      c.doStuff();
   }
}
