/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package a.org.fakereplace.test.replacement.instancefield;

import java.lang.reflect.Field;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
