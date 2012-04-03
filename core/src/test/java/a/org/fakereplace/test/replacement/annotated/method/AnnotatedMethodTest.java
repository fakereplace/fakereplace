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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnnotatedMethodTest {

    @BeforeClass
    public static void setup() {
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
        Assert.assertEquals("1", m1.getAnnotation(MethodAnnotation.class).value());
        Assert.assertFalse(m2.isAnnotationPresent(MethodAnnotation.class));
        Assert.assertEquals("3", m3.getAnnotation(MethodAnnotation.class).value());
        Assert.assertFalse(m4.isAnnotationPresent(MethodAnnotation.class));
    }

    @Test
    public void testAnnotatedElementGetDeclaredAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        Assert.assertEquals(1, getDeclaredAnnotations(m1).length);
        Assert.assertEquals(MethodAnnotation.class, getDeclaredAnnotations(m1)[0].annotationType());

        Assert.assertEquals(0, getDeclaredAnnotations(m2).length);

        Assert.assertEquals(1 ,getDeclaredAnnotations(m3).length);
        Assert.assertEquals(MethodAnnotation.class, getDeclaredAnnotations(m3)[0].annotationType() );

        Assert.assertEquals(0, getDeclaredAnnotations(m4).length);
    }

    @Test
    public void testMethodAnnotationGetDeclaredAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        Assert.assertEquals(1, m1.getDeclaredAnnotations().length);
        Assert.assertEquals(MethodAnnotation.class, m1.getDeclaredAnnotations()[0].annotationType() );

        Assert.assertEquals(0, m2.getDeclaredAnnotations().length);

        Assert.assertEquals(1, m3.getDeclaredAnnotations().length);
        Assert.assertEquals(MethodAnnotation.class, m3.getDeclaredAnnotations()[0].annotationType() );

        Assert.assertEquals(0, m4.getDeclaredAnnotations().length);
    }

    @Test
    public void testAnnotatedElementGetAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        Assert.assertEquals(1, getAnnotations(m1).length);
        Assert.assertEquals(MethodAnnotation.class, getAnnotations(m1)[0].annotationType());

        Assert.assertEquals(0, getAnnotations(m2).length);

        Assert.assertEquals(1, getAnnotations(m3).length);
        Assert.assertEquals(MethodAnnotation.class, getAnnotations(m3)[0].annotationType());

        Assert.assertEquals(0, getAnnotations(m4).length);
    }

    @Test
    public void testMethodAnnotationGetAnnotations() throws SecurityException, NoSuchMethodException {
        Method m1 = MethodAnnotated.class.getMethod("method1");
        Method m2 = MethodAnnotated.class.getMethod("method2");
        Method m3 = MethodAnnotated.class.getMethod("method3");
        Method m4 = MethodAnnotated.class.getMethod("method4");

        Assert.assertEquals(1, m1.getAnnotations().length);
        Assert.assertEquals(MethodAnnotation.class, m1.getAnnotations()[0].annotationType());

        Assert.assertEquals(0, m2.getAnnotations().length);

        Assert.assertEquals(1, m3.getAnnotations().length);
        Assert.assertEquals(MethodAnnotation.class, m3.getAnnotations()[0].annotationType());

        Assert.assertEquals(0, m4.getAnnotations().length);
    }

    public java.lang.annotation.Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    public java.lang.annotation.Annotation[] getAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

}
