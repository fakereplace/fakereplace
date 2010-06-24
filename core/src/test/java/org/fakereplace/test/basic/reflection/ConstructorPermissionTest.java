package org.fakereplace.test.basic.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fakereplace.test.coverage.ChangeTestType;
import org.fakereplace.test.coverage.CodeChangeType;
import org.fakereplace.test.coverage.Coverage;
import org.fakereplace.test.coverage.MultipleCoverage;
import org.testng.annotations.Test;

public class ConstructorPermissionTest
{

   public ConstructorPermissionTest()
   {

   }

   @Test
   @MultipleCoverage( {
         @Coverage(privateMember = false, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.GET_DECLARED_BY_NAME),
         @Coverage(privateMember = false, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.INVOKE_BY_REFLECTION) })
   public void testConstructorPermissions() throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException
   {
      Constructor<? extends ConstructorPermissionTest> m = getClass().getDeclaredConstructor();
      m.newInstance();
   }

   @MultipleCoverage( {
         @Coverage(privateMember = true, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.GET_DECLARED_BY_NAME),
         @Coverage(privateMember = true, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.INVOKE_BY_REFLECTION) })
   @Test(expectedExceptions = IllegalAccessException.class)
   public void testConstructorPermissionsOnOtherClass() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
         InstantiationException
   {
      Constructor<ConstructorPermissionBean> m = ConstructorPermissionBean.class.getDeclaredConstructor();
      m.newInstance();
   }

}
