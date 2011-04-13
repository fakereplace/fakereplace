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

package a.org.fakereplace.test.replacement.interfacemethod;

import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InterfaceMethodTest {
    @BeforeClass(groups = "interface")
    public void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(InterfaceCallingClass.class, InterfaceCallingClass1.class);
        rep.queueClassForReplacement(SomeInterface.class, SomeInterface1.class);
        rep.queueClassForReplacement(ImplementingClass.class, ImplementingClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test(groups = "interface")
    public void testAddingInterfaceMethod() {
        SomeInterface iface = new ImplementingClass();
        InterfaceCallingClass caller = new InterfaceCallingClass();
        assert caller.call(iface).equals("added");
    }

    @Test(groups = "interface")
    public void testAddingInterfaceMethodByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = SomeInterface.class.getDeclaredMethod("added");
        ImplementingClass cls = new ImplementingClass();
        assert method.invoke(cls).equals("added");
    }
}
