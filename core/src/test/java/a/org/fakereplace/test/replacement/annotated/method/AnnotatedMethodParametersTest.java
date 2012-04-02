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

package a.org.fakereplace.test.replacement.annotated.method;

import java.lang.reflect.Method;

import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AnnotatedMethodParametersTest {

    @BeforeClass
    public void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(MethodParameterAnnotated.class, MethodParameterAnnotated1.class);
        r.replaceQueuedClasses();

    }

    @Test
    public void testMethodParameterAnnotations() throws SecurityException, NoSuchMethodException {

        Method m1 = MethodParameterAnnotated.class.getMethod("method1", int.class);
        Method m2 = MethodParameterAnnotated.class.getMethod("method2", int.class);
        Method m3 = MethodParameterAnnotated.class.getMethod("method3", int.class);

        assert ((MethodAnnotation) m1.getParameterAnnotations()[0][0]).value().equals("1");
        assert m2.getParameterAnnotations()[0].length == 0;
        assert ((MethodAnnotation) m3.getParameterAnnotations()[0][0]).value().equals("3");

    }

}
