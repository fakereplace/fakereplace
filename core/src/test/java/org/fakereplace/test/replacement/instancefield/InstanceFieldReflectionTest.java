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

package org.fakereplace.test.replacement.instancefield;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class InstanceFieldReflectionTest {

    @BeforeClass(groups = "instanceFieldByReflection")
    public void setup() {
        ClassReplacer c = new ClassReplacer();
        c.queueClassForReplacement(InstanceFieldReflection.class, InstanceFieldReflection1.class);
        c.replaceQueuedClasses();
    }

    @Test(groups = "instanceFieldByReflection")
    public void testSettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("value");
        field.set(r, "hello world");
        assert r.getValue().equals("hello world");
    }

    @Test(groups = "instanceFieldByReflection")
    public void testSettingPrimitiveFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("intValue");
        field.setInt(r, 10);
        Assert.assertEquals(r.getIntValue(), 10);
        Assert.assertEquals(field.getInt(r), 10);
    }

    @Test(groups = "instanceFieldByReflection")
    public void testSettingWideFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("longValue");
        field.setLong(r, 10L);
        Assert.assertEquals(r.getLongValue(), 10);
        Assert.assertEquals(field.getLong(r), 10);
    }

    @Test(groups = "instanceFieldByReflection")
    public void testGettingInstanceFieldByReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("value");
        field.get(r);
        assert r.getValue().equals("hi");
    }

    @Test(groups = "instanceFieldByReflection")
    public void testPublicAddedFieldsAccessible() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getField("vis");
    }

    @Test(groups = "instanceFieldByReflection", expectedExceptions = NoSuchFieldException.class)
    public void testPrivateAddedFieldsNotAccessibleThroughGetField() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getField("hid");
    }

    @Test(groups = "instanceFieldByReflection")
    public void testPrivateAddedFieldsAccessibleThroughGetDeclaredField() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceFieldReflection r = new InstanceFieldReflection();
        Field field = InstanceFieldReflection.class.getDeclaredField("hid");
    }

    public void testPublicAddedFieldAccessibleThroughGetFields() {
        boolean found = false;
        for (Field f : InstanceFieldReflection.class.getFields()) {
            if (f.getName().equals("vis")) {
                found = true;
            }
        }
        assert found;
    }

    public void testPrivateAddedFieldNotAccessibleThroughGetFields() {
        boolean found = false;
        for (Field f : InstanceFieldReflection.class.getFields()) {
            if (f.getName().equals("hid")) {
                found = true;
            }
        }
        assert !found;
    }

    public void testPrivateAddedFieldAccessibleThroughGetDeclaredFields() {
        boolean found = false;
        for (Field f : InstanceFieldReflection.class.getDeclaredFields()) {
            if (f.getName().equals("hid")) {
                found = true;
                assert f.isAnnotationPresent(SomeAnnotation.class);
            }
        }
        assert found;
    }

}
