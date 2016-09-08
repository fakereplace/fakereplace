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

package a.org.fakereplace.test.replacement.constructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

import a.org.fakereplace.test.replacement.constructor.other.Creator;
import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConstructorTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ConstructorClass.class, ConstructorClass1.class);
        rep.queueClassForReplacement(ConstructorCallingClass.class, ConstructorCallingClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test
    public void testConstructor() {
        Assert.assertEquals("b", ConstructorCallingClass.getInstance().getValue());
    }

    @Test
    public void testGetDeclaredConstructors() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        boolean c1 = false, c2 = false;
        for (Constructor<?> c : ConstructorClass.class.getDeclaredConstructors()) {
            if (c.getParameterTypes().length == 1) {
                if (c.getParameterTypes()[0] == List.class) {
                    c1 = true;
                } else if (c.getParameterTypes()[0] == String.class) {
                    c2 = true;
                }
            }
        }
        Assert.assertTrue(c1);
        Assert.assertTrue(c2);
    }

    @Test
    public void testGetConstructors() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        boolean c1 = false, c2 = false;
        for (Constructor<?> c : ConstructorClass.class.getConstructors()) {
            if (c.getParameterTypes().length == 1) {
                if (c.getParameterTypes()[0] == List.class) {
                    c1 = true;
                } else if (c.getParameterTypes()[0] == String.class) {
                    c2 = true;
                }
            }
        }
        Assert.assertFalse(c1);
        Assert.assertTrue(c2);
    }

    @Test
    public void testConstructorByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Class<?> c = ConstructorClass.class;
        Constructor<?> con = c.getDeclaredConstructor(List.class);
        con.setAccessible(true);
        ConstructorClass inst = (ConstructorClass) con.newInstance(null, null);
        Assert.assertEquals("h", inst.getValue());
    }

    @Test(expected = IllegalAccessException.class)
    public void testConstructorByReflectionWithException() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        Class<?> c = ConstructorClass.class;
        Constructor<?> con = c.getDeclaredConstructor(Set.class);
        ConstructorClass inst = (ConstructorClass) con.newInstance(null, null);
    }

    @Test
    public void testVirtualConstrcutorGenericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = ConstructorClass.class;
        Constructor<?> con = c.getDeclaredConstructor(List.class);
        Assert.assertEquals(String.class, ((ParameterizedType) con.getGenericParameterTypes()[0]).getActualTypeArguments()[0]);
    }

    @Test
    public void testPackagePrivateConstructor() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Creator c = new Creator();
        c.doStuff();
    }

    @Test
    public void testReplacementOrder() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Assert.assertEquals("old", new ConstructorOrderCaller().getOrder().getData());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ConstructorOrderCaller.class, ConstructorOrderCaller1.class);
        rep.rewriteNames(ConstructorOrderClass.class, ConstructorOrderClass1.class);
        rep.replaceQueuedClasses();
        try {
            String res = new ConstructorOrderCaller().getOrder().getData();
            Assert.fail(res);
        } catch (NoSuchMethodError expected) {
        }
        rep.queueClassForReplacement(ConstructorOrderClass.class, ConstructorOrderClass1.class);
        rep.replaceQueuedClasses();
        Assert.assertEquals("new", new ConstructorOrderCaller().getOrder().getData());
    }
}
