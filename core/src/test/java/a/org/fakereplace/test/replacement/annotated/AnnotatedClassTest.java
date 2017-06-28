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

package a.org.fakereplace.test.replacement.annotated;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class AnnotatedClassTest {

    @Test
    public void testStringA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(StringC.class, StringC1.class);
        rep.replaceQueuedClasses();
        StringC ns = new StringC();
        Class c = StringC.class;
        Assert.assertFalse(ns.getClass().isAnnotationPresent(StringA.class));
        Field field = c.getDeclaredField("field");
        Assert.assertTrue(field.isAnnotationPresent(StringA.class));
        Assert.assertEquals("1", field.getAnnotation(StringA.class).value());

    }

    @Test
    public void testIntA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(IntC.class, IntC1.class);
        rep.replaceQueuedClasses();
        IntC ns = new IntC();
        Class c = IntC.class;
        Assert.assertFalse(ns.getClass().isAnnotationPresent(IntA.class));
        Field field = c.getDeclaredField("field");
        Assert.assertTrue(field.isAnnotationPresent(IntA.class));
        Assert.assertEquals(2, field.getAnnotation(IntA.class).value());

    }

    @Test
    public void testClassA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ClassC.class, ClassC1.class);
        rep.replaceQueuedClasses();
        ClassC ns = new ClassC();
        Class c = ClassC.class;
        Assert.assertFalse(ns.getClass().isAnnotationPresent(ClassA.class));
        Field field = c.getDeclaredField("field");
        Assert.assertTrue(field.isAnnotationPresent(ClassA.class));
        Assert.assertEquals(Integer.class, field.getAnnotation(ClassA.class).value());

    }

    @Test
    public void testAnnotationA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(AnnotationC.class, AnnotationC1.class);
        rep.replaceQueuedClasses();
        AnnotationC ns = new AnnotationC();
        Class c = AnnotationC.class;
        Assert.assertFalse(ns.getClass().isAnnotationPresent(AnnotationA.class));
        Field field = c.getDeclaredField("field");
        Assert.assertTrue(field.isAnnotationPresent(AnnotationA.class));
        Assert.assertEquals("1", field.getAnnotation(AnnotationA.class).value().value());

    }

    @Test
    public void testAnnotationArrayA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(AnnotationArrayC.class, AnnotationArrayC1.class);
        rep.replaceQueuedClasses();
        AnnotationArrayC ns = new AnnotationArrayC();
        Class c = AnnotationArrayC.class;
        Assert.assertFalse(ns.getClass().isAnnotationPresent(AnnotationArrayA.class));
        Field field = c.getDeclaredField("field");
        Assert.assertTrue(field.isAnnotationPresent(AnnotationArrayA.class));
        StringA[] ar = field.getAnnotation(AnnotationArrayA.class).value();
        Assert.assertEquals("1", ar[0].value());
        Assert.assertEquals("2", ar[1].value());

    }
}
