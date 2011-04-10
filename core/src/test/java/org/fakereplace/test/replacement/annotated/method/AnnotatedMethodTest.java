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

package org.fakereplace.test.replacement.annotated.method;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class AnnotatedMethodTest {

    @BeforeClass
    public void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(MethodAnnotated.class, MethodAnnotated1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testMethodAnnotationGetAnnotation() throws SecurityException, NoSuchMethodException {

        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");
        assert m1.getAnnotation(MethodAnnotation.class).value().equals("1");
        assert !m2.isAnnotationPresent(MethodAnnotation.class);
        assert m3.getAnnotation(MethodAnnotation.class).value().equals("3");
        assert !m4.isAnnotationPresent(MethodAnnotation.class);
    }

    @Test
    public void testAnnotatedElementGetDeclaredAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        assert getDeclaredAnnotations(m1).length == 1 : m1.getDeclaredAnnotations().length;
        assert getDeclaredAnnotations(m1)[0].annotationType() == MethodAnnotation.class;

        assert getDeclaredAnnotations(m2).length == 0;

        assert getDeclaredAnnotations(m3).length == 1;
        assert getDeclaredAnnotations(m3)[0].annotationType() == MethodAnnotation.class;

        assert getDeclaredAnnotations(m4).length == 0;
    }

    @Test
    public void testMethodAnnotationGetDeclaredAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        assert m1.getDeclaredAnnotations().length == 1 : m1.getDeclaredAnnotations().length;
        assert m1.getDeclaredAnnotations()[0].annotationType() == MethodAnnotation.class;

        assert m2.getDeclaredAnnotations().length == 0;

        assert m3.getDeclaredAnnotations().length == 1;
        assert m3.getDeclaredAnnotations()[0].annotationType() == MethodAnnotation.class;

        assert m4.getDeclaredAnnotations().length == 0;
    }

    @Test
    public void testAnnotatedElementGetAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        assert getAnnotations(m1).length == 1 : m1.getDeclaredAnnotations().length;
        assert getAnnotations(m1)[0].annotationType() == MethodAnnotation.class;

        assert getAnnotations(m2).length == 0;

        assert getAnnotations(m3).length == 1;
        assert getAnnotations(m3)[0].annotationType() == MethodAnnotation.class;

        assert getAnnotations(m4).length == 0;
    }

    @Test
    public void testMethodAnnotationGetAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        assert m1.getAnnotations().length == 1 : m1.getDeclaredAnnotations().length;
        assert m1.getAnnotations()[0].annotationType() == MethodAnnotation.class;

        assert m2.getAnnotations().length == 0;

        assert m3.getAnnotations().length == 1;
        assert m3.getAnnotations()[0].annotationType() == MethodAnnotation.class;

        assert m4.getAnnotations().length == 0;
    }

    public java.lang.annotation.Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    public java.lang.annotation.Annotation[] getAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

}
