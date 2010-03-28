package org.fakereplace.test.basic.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.testng.annotations.Test;

public class ConstructorPermissionTest
{

   public ConstructorPermissionTest()
   {

   }

   @Test
   public void testConstructorPermissions() throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException
   {
      Constructor<? extends ConstructorPermissionTest> m = getClass().getDeclaredConstructor();
      m.newInstance();
   }

   @Test(expectedExceptions = IllegalAccessException.class)
   public void testConstructorPermissionsOnOtherClass() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException
   {
      Constructor<ConstructorPermissionBean> m = ConstructorPermissionBean.class.getDeclaredConstructor();
      m.newInstance();
   }

}
