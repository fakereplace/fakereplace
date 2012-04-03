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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Test;

public class ExceptionTableUpdateTest {
    @Test
    public void testExceptionTableCorrect() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassReplacer cr = new ClassReplacer();
        cr.queueClassForReplacement(VirtualMethodExceptionClass.class, VirtualMethodExceptionClass1.class);
        cr.replaceQueuedClasses();

        VirtualMethodExceptionClass i = new VirtualMethodExceptionClass();
        Method m = i.getClass().getMethod("doStuff1", int.class, int.class);
        m.invoke(i, 0, 0);
        m = i.getClass().getMethod("doStuff2", int.class, int.class);
        m.invoke(i, 0, 0);
    }

}
