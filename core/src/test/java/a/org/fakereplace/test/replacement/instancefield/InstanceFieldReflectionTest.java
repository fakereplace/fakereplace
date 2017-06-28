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

package a.org.fakereplace.test.replacement.instancefield;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class InstanceFieldReflectionTest {

    @BeforeClass
    public static void setup() {
        ClassReplacer c = new ClassReplacer();
        c.queueClassForReplacement(InstanceFieldReflection.class, InstanceFieldReflection1.class);
        c.replaceQueuedClasses();
    }

    @Test
    public void testSettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("value");
        field.set(r, "hello world");
        Assert.assertEquals("hello world", r.getValue());
    }

    @Test
    public void testSettingPrimitiveFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("intValue");
        field.setInt(r, 10);
        Assert.assertEquals(10, r.getIntValue());
        Assert.assertEquals(10, field.getInt(r));
    }

    @Test
    public void testSettingWideFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("longValue");
        field.setLong(r, 10L);
        Assert.assertEquals(10, r.getLongValue());
        Assert.assertEquals(10, field.getLong(r));
    }

    @Test
    public void testGettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("value");
        field.get(r);
        Assert.assertEquals("hi", r.getValue());
    }

    @Test
    public void testPublicAddedFieldsAccessible() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getField("vis");
    }

    @Test(expected = NoSuchFieldException.class)
    public void testPrivateAddedFieldsNotAccessibleThroughGetField() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getField("hid");
    }

    @Test
    public void testPrivateAddedFieldsAccessibleThroughGetDeclaredField() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("hid");
    }

    @Test
    public void testSettingPrivateFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("privateField");
        field.setAccessible(true);
        field.setInt(r, 10);
        Assert.assertEquals(10, r.getPrivateField());
        Assert.assertEquals(10, field.getInt(r));
    }

    @Test(expected = IllegalAccessException.class)
    public void testSettingPrivateFieldByReflectionWithException() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("privateField");
        field.setInt(r, 10);
        Assert.assertEquals(10, r.getPrivateField());
        Assert.assertEquals(10, field.getInt(r));
    }

    @Test
    public void testPublicAddedFieldAccessibleThroughGetFields() {
        boolean found = false;
        for (Field f : InstanceFieldReflection.class.getFields()) {
            if (f.getName().equals("vis")) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testPrivateAddedFieldNotAccessibleThroughGetFields() {
        boolean found = false;
        for (Field f : InstanceFieldReflection.class.getFields()) {
            if (f.getName().equals("hid")) {
                found = true;
            }
        }
        Assert.assertFalse(found);
    }

    @Test
    public void testPrivateAddedFieldAccessibleThroughGetDeclaredFields() {
        boolean found = false;
        for (Field f : InstanceFieldReflection.class.getDeclaredFields()) {
            if (f.getName().equals("hid")) {
                found = true;
                Assert.assertTrue(f.isAnnotationPresent(SomeAnnotation.class));
            }
        }
        Assert.assertTrue(found);
    }

}
