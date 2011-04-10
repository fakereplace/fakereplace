package org.fakereplace.test.replacement.staticmethod;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StaticAccessingMethodTest {
    @BeforeClass(groups = "staticmethodaccess", dependsOnGroups = "staticmethod")
    public void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(StaticAccessingClass.class, StaticAccessingClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test(groups = "staticmethodaccess")
    public void testIntPrimitiveReturnType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("getInt");
        Integer res = (Integer) m.invoke(null);
        assert res == 10 : "Failed to replace static method with primitive return value";
    }

    @Test(groups = "staticmethodaccess")
    public void testLongPrimitiveReturnType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("getLong");
        Long res = (Long) m.invoke(null);
        assert res == 11 : "Failed to replace static method with primitive return value";
    }

    @Test(groups = "staticmethodaccess")
    public void testIntegerMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("integerAdd", Integer.class);
        Integer res = (Integer) m.invoke(null, new Integer(10));
        assert res == 11 : "Failed to replace static method with Object method parameter";
    }

    @Test(groups = "staticmethodaccess")
    public void testIntMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("intAdd", int.class);
        Integer res = (Integer) m.invoke(null, 10);
        assert res == 11;
    }

    @Test(groups = "staticmethodaccess")
    public void testShortMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("shortAdd", short.class);
        Short res = (Short) m.invoke(null, (short) 10);
        assert res == 11;
    }

    @Test(groups = "staticmethodaccess")
    public void testLongMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("longAdd", long.class);
        Long res = (Long) m.invoke(null, (long) 10);
        assert res == 11;
    }

    @Test(groups = "staticmethodaccess")
    public void testByteMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("byteAdd", byte.class);
        Byte res = (Byte) m.invoke(null, (byte) 10);
        assert res == 11;
    }

    @Test(groups = "staticmethodaccess")
    public void testFloatMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("floatAdd", float.class);
        Float res = (Float) m.invoke(null, 0.0f);
        assert res == 1;
    }

    @Test(groups = "staticmethodaccess")
    public void testDoubleMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("doubleAdd", double.class);
        Double res = (Double) m.invoke(null, 0.0f);
        assert res == 1;
    }

    @Test(groups = "staticmethodaccess")
    public void testCharMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("charAdd", char.class);
        Character res = (Character) m.invoke(null, 'a');
        assert res == 'b';
    }

    @Test(groups = "staticmethodaccess")
    public void testBooleanMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("negate", boolean.class);
        Boolean res = (Boolean) m.invoke(null, false);
        assert res.booleanValue();
    }
}
