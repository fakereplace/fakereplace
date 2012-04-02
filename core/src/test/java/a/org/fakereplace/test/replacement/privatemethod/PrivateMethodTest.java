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

package a.org.fakereplace.test.replacement.privatemethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PrivateMethodTest {
    @BeforeClass(groups = "privatemethod")
    public void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(PrivateMethodClass.class, PrivateMethodClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test(groups = "privatemethod")
    public void testAddingPrivateMethod() {
        PrivateMethodClass instance = new PrivateMethodClass();
        Assert.assertEquals(1, instance.getResult());
    }

    @Test(groups = "privatemethod")
    public void testAddingPrivateMethodByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = PrivateMethodClass.class.getDeclaredMethod("realResult");
        method.setAccessible(true);
        PrivateMethodClass cls = new PrivateMethodClass();
        Assert.assertEquals(1, method.invoke(cls));
    }
}
