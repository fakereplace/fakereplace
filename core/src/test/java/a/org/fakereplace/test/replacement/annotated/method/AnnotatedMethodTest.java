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
