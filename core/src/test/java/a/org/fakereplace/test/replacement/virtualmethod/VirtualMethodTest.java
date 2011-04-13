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

package a.org.fakereplace.test.replacement.virtualmethod;

import a.org.fakereplace.test.coverage.ChangeTestType;
import a.org.fakereplace.test.coverage.CodeChangeType;
import a.org.fakereplace.test.coverage.Coverage;
import a.org.fakereplace.test.coverage.MultipleCoverage;
import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VirtualMethodTest {
    @BeforeClass(groups = "virtualmethod")
    public void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(VirtualClass.class, VirtualClass1.class);
        rep.queueClassForReplacement(VirtualCaller.class, VirtualCaller1.class);
        rep.queueClassForReplacement(NoSupChild.class, NoSupChild1.class);
        rep.replaceQueuedClasses();
    }

    @Test(groups = "virtualmethod")
    public void testVirtualMethodByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        VirtualClass ns = new VirtualClass();
        Class c = VirtualClass.class;
        Method get = c.getMethod("getValue");

        Method add = c.getMethod("addValue", int.class);
        assert get != null;
        Integer res = (Integer) get.invoke(ns);
        assert res.equals(new Integer(1));
        add.invoke(ns, 1);
        res = (Integer) get.invoke(ns);
        assert res.equals(new Integer(3)) : "Expected 3 got " + res;

        Map<String, String> tmap = new HashMap<String, String>();
        tmap.put("a", "b");
        Set<String> tset = new HashSet<String>();
        tset.add("c");
        Method clear = c.getMethod("clearFunction", Map.class, Set.class, int.class);
        clear.invoke(ns, tmap, tset, 0);
        assert tmap.isEmpty();
        assert tset.isEmpty();

    }

    @Test(groups = "virtualmethod")
    public void testVirtualMethodChildByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        VirtualChild ns = new VirtualChild();
        Class c = VirtualChild.class;
        Method get = c.getMethod("getValue");

        Method add = c.getMethod("addValue", int.class);
        assert get != null;
        Integer res = (Integer) get.invoke(ns);
        assert res.equals(new Integer(1)) : " actual " + res;
        add.invoke(ns, 1);
        res = (Integer) get.invoke(ns);
        assert res.equals(new Integer(3)) : "Expected 3 got " + res;

    }

    @MultipleCoverage({@Coverage(change = CodeChangeType.ADD_INSTANCE_METHOD, privateMember = true, test = ChangeTestType.ACCESS_THROUGH_BYTECODE)})
    @Test(groups = "virtualmethod")
    public void testVirtualMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        VirtualClass ns = new VirtualClass();
        VirtualCaller caller = new VirtualCaller();
        caller.add(ns);
        int val = ns.getValue();
        assert val == 11 : "expected 10 got " + val;

        Map<String, String> tmap = new HashMap<String, String>();
        tmap.put("a", "b");
        Set<String> tset = new HashSet<String>();
        tset.add("c");
        ns.clear(tmap, tset);
        assert tmap.isEmpty();
        assert tset.isEmpty();

    }

    @Test(groups = "virtualmethod")
    public void testVirtualMethodModifiers() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = VirtualClass.class;
        Method add = c.getMethod("addValue", int.class);
        assert !Modifier.isStatic(add.getModifiers());
        add = c.getMethod("getStuff", List.class);
        assert !Modifier.isStatic(add.getModifiers());
        add = c.getMethod("getValue");
        assert !Modifier.isStatic(add.getModifiers());
    }

    @Test(groups = "virtualmethod")
    public void testVirtualMethodExceptionsByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = VirtualClass.class;
        Method add = c.getMethod("addValue", int.class);
        assert add.getExceptionTypes()[0].equals(ArithmeticException.class);
    }

    @Test(groups = "virtualmethod")
    public void testVirtualMethodGernericTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Class<?> c = VirtualClass.class;
        Method meth = c.getMethod("getStuff", List.class);
        assert ((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()[0].equals(String.class);
    }

    @Test(groups = "virtualmethod")
    public void testVirtualMethodGernericParameterTypeByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = VirtualClass.class;
        Method meth = c.getMethod("getStuff", List.class);
        assert ((ParameterizedType) meth.getGenericParameterTypes()[0]).getActualTypeArguments()[0].equals(Integer.class);
    }

    @MultipleCoverage({
            @Coverage(change = CodeChangeType.ADD_INSTANCE_METHOD, privateMember = false, test = ChangeTestType.GET_DECLARED_ALL),
            @Coverage(change = CodeChangeType.ADD_INSTANCE_METHOD, privateMember = true, test = ChangeTestType.GET_DECLARED_ALL)})
    @Test(groups = "virtualmethod")
    public void testVirtualMethodgetDeclaredMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean add = false;
        boolean stuff = false;
        boolean priv = false;
        Class<?> c = VirtualClass.class;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("addValue")) {
                assert m.getParameterTypes()[0] == int.class;
                add = true;
            }
            if (m.getName().equals("getStuff")) {
                assert m.getParameterTypes()[0] == List.class;
                stuff = true;
            }
            if (m.getName().equals("privateFunction")) {
                priv = true;
            }
        }
        assert add;
        assert stuff;
        assert priv;
    }

    @Test(groups = "virtualmethod")
    public void testVirtualChildMethodgetMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean add = false;
        boolean stuff = false;
        Class<?> c = VirtualChild.class;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("addValue")) {
                assert m.getParameterTypes()[0] == int.class;
                add = true;
            }
            if (m.getName().equals("getStuff")) {
                assert m.getParameterTypes()[0] == List.class;
                stuff = true;
            }
        }
        assert add;
        assert stuff;
    }

    @MultipleCoverage({
            @Coverage(change = CodeChangeType.ADD_INSTANCE_METHOD, privateMember = false, test = ChangeTestType.GET_ALL),
            @Coverage(change = CodeChangeType.ADD_INSTANCE_METHOD, privateMember = true, test = ChangeTestType.GET_ALL)})
    @Test(groups = "virtualmethod")
    public void testVirtualMethodgetMethods() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean add = false;
        boolean stuff = false;
        boolean priv = false;
        Class<?> c = VirtualClass.class;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("addValue")) {
                assert m.getParameterTypes()[0] == int.class;
                add = true;
            }
            if (m.getName().equals("getStuff")) {
                assert m.getParameterTypes()[0] == List.class;
                stuff = true;
            }
            if (m.getName().equals("privateFunction")) {
                priv = true;
            }
        }
        assert add;
        assert stuff;
        assert !priv;
    }

    @Test(groups = "virtualmethod", enabled = false)
    public void testToStringOverride() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        VirtualClass c = new VirtualClass();
        Assert.assertEquals(getString(c), "VirtualChild1");
    }

    @Test(groups = "virtualmethod")
    public void testAddedDelegateMethodsDoNotBreakOnReplacement() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        VirtualCaller c = new VirtualCaller();
        assert c.toString().contains("VirtualCaller") : c.toString();
    }

    @Test(groups = "virtualmethod")
    public void testOverrideWithSuperclassNotLoaded() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        NoSupClass c = new NoSupChild();
        assert c.getStuff(0, 0, "", 0, 0).equals("NoSupChild") : "Expected NoSupChild got:" + c.getStuff(0, 0, "", 0, 0);
    }

    @Test(groups = "virtualmethod")
    public void testOverrideWithSuperclassNotLoadedSuperclassNotChanged() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        NoSupClass c = new NoSupClass();
        assert c.getStuff(0, 0, "", 0, 0).equals("NoSupClass") : "Expected NoSupClass got:" + c.getStuff(0, 0, "", 0, 0);
    }

    @Test(groups = "virtualmethod")
    public void testOverrideWithSuperclassNotLoadedOtherChildNotChanged() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        NoSupUnmodifiedChild c = new NoSupUnmodifiedChild();
        assert c.getStuff(0, 0, "", 0, 0).equals("NoSupUnmodifiedChild") : "Expected NoSupUnmodifiedChild got:" + c.getStuff(0, 0, "", 0, 0);
    }

    public String getString(Object o) {
        return o.toString();
    }

}
