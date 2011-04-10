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

package org.fakereplace.test.replacement.annotated;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class AnnotatedClassTest {

    @Test(groups = "annotatedclass")
    public void testStringA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(StringC.class, StringC1.class);
        rep.replaceQueuedClasses();
        StringC ns = new StringC();
        Class c = StringC.class;
        assert !ns.getClass().isAnnotationPresent(StringA.class);
        Field field = c.getDeclaredField("field");
        assert field.isAnnotationPresent(StringA.class);
        assert field.getAnnotation(StringA.class).value().equals("1");

    }

    @Test(groups = "annotatedclass")
    public void testIntA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(IntC.class, IntC1.class);
        rep.replaceQueuedClasses();
        IntC ns = new IntC();
        Class c = IntC.class;
        assert !ns.getClass().isAnnotationPresent(IntA.class);
        Field field = c.getDeclaredField("field");
        assert field.isAnnotationPresent(IntA.class);
        assert field.getAnnotation(IntA.class).value() == 2;

    }

    @Test(groups = "annotatedclass")
    public void testClassA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ClassC.class, ClassC1.class);
        rep.replaceQueuedClasses();
        ClassC ns = new ClassC();
        Class c = ClassC.class;
        assert !ns.getClass().isAnnotationPresent(ClassA.class);
        Field field = c.getDeclaredField("field");
        assert field.isAnnotationPresent(ClassA.class);
        assert field.getAnnotation(ClassA.class).value() == Integer.class;

    }

    @Test(groups = "annotatedclass")
    public void testAnnotationA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(AnnotationC.class, AnnotationC1.class);
        rep.replaceQueuedClasses();
        AnnotationC ns = new AnnotationC();
        Class c = AnnotationC.class;
        assert !ns.getClass().isAnnotationPresent(AnnotationA.class);
        Field field = c.getDeclaredField("field");
        assert field.isAnnotationPresent(AnnotationA.class);
        assert field.getAnnotation(AnnotationA.class).value().value().equals("1");

    }

    @Test(groups = "annotatedclass")
    public void testAnnotationArrayA() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(AnnotationArrayC.class, AnnotationArrayC1.class);
        rep.replaceQueuedClasses();
        AnnotationArrayC ns = new AnnotationArrayC();
        Class c = AnnotationArrayC.class;
        assert !ns.getClass().isAnnotationPresent(AnnotationArrayA.class);
        Field field = c.getDeclaredField("field");
        assert field.isAnnotationPresent(AnnotationArrayA.class);
        StringA[] ar = field.getAnnotation(AnnotationArrayA.class).value();
        assert ar[0].value().equals("1");
        assert ar[1].value().equals("2");

    }
}
