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

package a.org.fakereplace.test.basic.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fakereplace.boot.Constants;
import org.fakereplace.util.NoInstrument;
import org.junit.Test;

public class MethodTest {
    @Test
    public void testDelegatorMethodAdded() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException {
        TestRunner.runTest();
    }

    @Test
    public void testGetDeclaredMethods() {
        DoStuff d = new DoStuff();
        Method[] meths = d.getClass().getDeclaredMethods();
        for (Method m : meths) {
            if (m.getName().equals(Constants.ADDED_METHOD_NAME) || m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME)) {
                throw new RuntimeException("Added method delegator showing up in declared methods");
            }
        }
    }

    @NoInstrument
    private static class TestRunner {
        public static void runTest() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            DoStuff d = new DoStuff();
            Method m = d.getClass().getMethod(Constants.ADDED_METHOD_NAME, int.class, Object[].class);
            m.invoke(d, 10, null);
        }
    }

}
