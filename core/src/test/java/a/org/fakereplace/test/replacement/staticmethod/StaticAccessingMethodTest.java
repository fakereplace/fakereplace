/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package a.org.fakereplace.test.replacement.staticmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StaticAccessingMethodTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(StaticAccessingClass.class, StaticAccessingClass1.class);
        rep.queueClassForReplacement(StaticClass.class, StaticClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test
    public void testIntPrimitiveReturnType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("getInt");
        Integer res = (Integer) m.invoke(null);
        Assert.assertEquals((Integer) 10, res);
    }

    @Test
    public void testLongPrimitiveReturnType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("getLong");
        Long res = (Long) m.invoke(null);
        Assert.assertEquals((Long) 11l, res);
    }

    @Test
    public void testIntegerMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("integerAdd", Integer.class);
        Integer res = (Integer) m.invoke(null, new Integer(10));
        Assert.assertEquals((Integer) 11, res);
    }

    @Test
    public void testIntMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("intAdd", int.class);
        Integer res = (Integer) m.invoke(null, 10);
        Assert.assertEquals((Integer) 11, res);
    }

    @Test
    public void testShortMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("shortAdd", short.class);
        Short res = (Short) m.invoke(null, (short) 10);
        Assert.assertEquals((Short) (short) 11, res);
    }

    @Test
    public void testLongMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("longAdd", long.class);
        Long res = (Long) m.invoke(null, (long) 10);
        Assert.assertEquals((Long) 11l, res);
    }

    @Test
    public void testByteMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("byteAdd", byte.class);
        Byte res = (Byte) m.invoke(null, (byte) 10);
        Assert.assertEquals((Byte) (byte) 11, res);
    }

    @Test
    public void testFloatMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("floatAdd", float.class);
        Float res = (Float) m.invoke(null, 0.0f);
        Assert.assertEquals((Float) 1f, res);
    }

    @Test
    public void testDoubleMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("doubleAdd", double.class);
        Double res = (Double) m.invoke(null, 0.0f);
        Assert.assertEquals((Double) 1.0, res);
    }

    @Test
    public void testCharMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("charAdd", char.class);
        Character res = (Character) m.invoke(null, 'a');
        Assert.assertEquals((Object) 'b', res);
    }

    @Test
    public void testBooleanMethodParameter() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StaticAccessingClass ns = new StaticAccessingClass();
        Class c = StaticAccessingClass.class;
        Method m = c.getMethod("negate", boolean.class);
        Boolean res = (Boolean) m.invoke(null, false);
        Assert.assertTrue(res.booleanValue());
    }
}
