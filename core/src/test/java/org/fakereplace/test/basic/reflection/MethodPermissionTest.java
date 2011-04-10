package org.fakereplace.test.basic.reflection;

import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodPermissionTest {

    @Test
    public void testMethodPermissions() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method m = getClass().getDeclaredMethod("method");
        m.invoke(this);
    }

    @Test(expectedExceptions = IllegalAccessException.class)
    public void testMethodPermissionsOnOtherClass() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method m = MethodPermissionBean.class.getDeclaredMethod("method");
        m.invoke(null);
    }

    protected void method() {

    }

}
