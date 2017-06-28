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

package a.org.fakereplace.test.replacement.virtualmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class VirtualMethodTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(VirtualClass.class, VirtualClass1.class);
        rep.replaceQueuedClasses();
        rep.queueClassForReplacement(VirtualCaller.class, VirtualCaller1.class);
        rep.replaceQueuedClasses();
        rep.queueClassForReplacement(NoSupChild.class, NoSupChild1.class);
        rep.replaceQueuedClasses();
    }

    @Test
    public void testVirtualMethodByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        VirtualClass ns = new VirtualClass();
        Class c = VirtualClass.class;
        Method get = c.getMethod("getValue");

        Method add = c.getMethod("addValue", int.class);
        Assert.assertNotNull(get);
        Integer res = (Integer) get.invoke(ns);
        Assert.assertEquals(Integer.valueOf(1), res);
        add.invoke(ns, 1);
        res = (Integer) get.invoke(ns);
        Assert.assertEquals(Integer.valueOf(3), res);

        Map<String, String> tmap = new HashMap<String, String>();
        tmap.put("a", "b");
        Set<String> tset = new HashSet<String>();
        tset.add("c");
        Method clear = c.getMethod("clearFunction", Map.class, Set.class, int.class);
        clear.invoke(ns, tmap, tset, 0);
        Assert.assertTrue(tmap.isEmpty());
        Assert.assertTrue(tset.isEmpty());

    }

    @Test
    public void testVirtualMethodChildByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        VirtualChild ns = new VirtualChild();
        Class c = VirtualChild.class;
        Method get = c.getMethod("getValue");

        Method add = c.getMethod("addValue", int.class);
        Assert.assertNotNull(get);
        Integer res = (Integer) get.invoke(ns);
        Assert.assertEquals(Integer.valueOf(1), res);
        add.invoke(ns, 1);
        res = (Integer) get.invoke(ns);
        Assert.assertEquals(Integer.valueOf(3), res);

    }

    @Test
    public void testVirtualMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        VirtualClass ns = new VirtualClass();
        VirtualCaller caller = new VirtualCaller();
        caller.add(ns);
        int val = ns.getValue();
        Assert.assertEquals(11, val);

        Map<String, String> tmap = new HashMap<String, String>();
        tmap.put("a", "b");
        Set<String> tset = new HashSet<String>();
        tset.add("c");
        ns.clear(tmap, tset);
        Assert.assertEquals(Collections.emptyMap(), tmap);
        Assert.assertEquals(Collections.emptySet(), tset);

    }

    @Test
    public void testVirtualMethodModifiers() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = VirtualClass.class;
        Method add = c.getMethod("addValue", int.class);
        Assert.assertTrue(!Modifier.isStatic(add.getModifiers()));
        add = c.getMethod("getStuff", List.class);
        Assert.assertTrue(!Modifier.isStatic(add.getModifiers()));
        add = c.getMethod("getValue");
        Assert.assertTrue(!Modifier.isStatic(add.getModifiers()));
    }

    @Test
    public void testVirtualMethodExceptionsByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = VirtualClass.class;
        Method add = c.getMethod("addValue", int.class);
        Assert.assertEquals(ArithmeticException.class, add.getExceptionTypes()[0]);
    }

    @Test
    public void testVirtualMethodGernericTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Class<?> c = VirtualClass.class;
        Method meth = c.getMethod("getStuff", List.class);
        Assert.assertEquals(String.class, ((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()[0]);
    }

    @Test
    public void testVirtualMethodGernericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = VirtualClass.class;
        Method meth = c.getMethod("getStuff", List.class);
        Assert.assertEquals(Integer.class, ((ParameterizedType) meth.getGenericParameterTypes()[0]).getActualTypeArguments()[0]);
    }

    @Test
    public void testVirtualMethodgetDeclaredMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean add = false;
        boolean stuff = false;
        boolean priv = false;
        Class<?> c = VirtualClass.class;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("addValue")) {
                Assert.assertEquals(int.class, m.getParameterTypes()[0]);
                add = true;
            }
            if (m.getName().equals("getStuff")) {
                Assert.assertEquals(List.class, m.getParameterTypes()[0]);
                stuff = true;
            }
            if (m.getName().equals("privateFunction")) {
                priv = true;
            }
        }
        Assert.assertTrue(add);
        Assert.assertTrue(stuff);
        Assert.assertTrue(priv);
    }

    @Test
    public void testVirtualChildMethodgetMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean add = false;
        boolean stuff = false;
        Class<?> c = VirtualChild.class;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("addValue")) {
                Assert.assertEquals(int.class, m.getParameterTypes()[0]);
                add = true;
            }
            if (m.getName().equals("getStuff")) {
                Assert.assertEquals(List.class, m.getParameterTypes()[0]);
                stuff = true;
            }
        }
        Assert.assertTrue(add);
        Assert.assertTrue(stuff);
    }

    @Test
    public void testVirtualMethodgetMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean add = false;
        boolean stuff = false;
        boolean priv = false;
        Class<?> c = VirtualClass.class;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("addValue")) {
                Assert.assertEquals(int.class, m.getParameterTypes()[0]);
                add = true;
            }
            if (m.getName().equals("getStuff")) {
                Assert.assertEquals(List.class, m.getParameterTypes()[0]);
                stuff = true;
            }
            if (m.getName().equals("privateFunction")) {
                priv = true;
            }
        }
        Assert.assertTrue(add);
        Assert.assertTrue(stuff);
        Assert.assertTrue(!priv);
    }

    @Ignore
    @Test
    public void testToStringOverride() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        VirtualClass c = new VirtualClass();
        Assert.assertEquals("VirtualChild1", getString(c));
    }

    @Test
    public void testAddedDelegateMethodsDoNotBreakOnReplacement() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        VirtualCaller c = new VirtualCaller();
        Assert.assertTrue(c.toString().contains("VirtualCaller"));
    }

    @Test
    public void testOverrideWithSuperclassNotLoaded() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        NoSupClass c = new NoSupChild();
        Assert.assertEquals("NoSupChild", c.getStuff(0, 0, "", 0, 0));
    }

    @Test
    public void testOverrideWithSuperclassNotLoadedSuperclassNotChanged() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        NoSupClass c = new NoSupClass();
        Assert.assertEquals("NoSupClass", c.getStuff(0, 0, "", 0, 0));
    }

    @Test
    public void testOverrideWithSuperclassNotLoadedOtherChildNotChanged() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        NoSupUnmodifiedChild c = new NoSupUnmodifiedChild();
        Assert.assertEquals("NoSupUnmodifiedChild", c.getStuff(0, 0, "", 0, 0));
    }

    @Test
    public void testReplacementOrder() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassReplacer cr = new ClassReplacer();
        OrderCaller orderCaller = new OrderCaller();
        Assert.assertEquals("bye", orderCaller.getMessage());
        cr.queueClassForReplacement(OrderCaller.class, OrderCaller1.class);
        cr.rewriteNames(OrderClass.class, OrderClass1.class);
        cr.replaceQueuedClasses();
        try {
            orderCaller.getMessage();
            Assert.fail();
        } catch (NoSuchMethodError expected) {}
        cr.queueClassForReplacement(OrderClass.class, OrderClass1.class);
        cr.replaceQueuedClasses();
        Assert.assertEquals("hello", orderCaller.getMessage());
    }

    public String getString(Object o) {
        return o.toString();
    }

}
