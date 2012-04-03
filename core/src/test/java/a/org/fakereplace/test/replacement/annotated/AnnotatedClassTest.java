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

package a.org.fakereplace.test.replacement.annotated;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.Test;

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
