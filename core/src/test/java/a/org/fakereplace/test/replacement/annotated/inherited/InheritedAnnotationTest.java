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

package a.org.fakereplace.test.replacement.annotated.inherited;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InheritedAnnotationTest {

    @BeforeClass
    public static void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(AddedInheritedSuperClass.class, AddedInheritedSuperClass1.class);
        r.queueClassForReplacement(RemovedInheritedSuperClass.class, RemovedInheritedSuperClass1.class);
        r.replaceQueuedClasses();
    }

    /**
     * test the annotation inheritence works the way it is supposed to
     */
    @Test
    public void testInheritedBehaviorWithoutReplacement() {
        Assert.assertTrue(InheritChild.class.isAnnotationPresent(InheritedAnnotation.class));
        Assert.assertFalse(InheritChild.class.isAnnotationPresent(NotInheritedAnnotation.class));

        Assert.assertEquals(10, InheritChild.class.getAnnotation(InheritedAnnotation.class).value());

        Assert.assertEquals(1, InheritChild.class.getAnnotations().length);
        Assert.assertEquals(InheritedAnnotation.class, InheritChild.class.getAnnotations()[0].annotationType());

        Assert.assertEquals(0, InheritChild.class.getDeclaredAnnotations().length);
    }

    /**
     * test the annotation inheritence works the way it is supposed to if a
     * superclass
     * has an inherited method added
     */
    @Test
    public void testAddedInheritedAnnotationBehavoir() {
        Assert.assertTrue(AddedInheritedChild.class.isAnnotationPresent(InheritedAnnotation.class));
        Assert.assertFalse(AddedInheritedChild.class.isAnnotationPresent(NotInheritedAnnotation.class));

        Assert.assertEquals(20, AddedInheritedChild.class.getAnnotation(InheritedAnnotation.class).value());

        Assert.assertEquals(1, AddedInheritedChild.class.getAnnotations().length);
        Assert.assertEquals(InheritedAnnotation.class, AddedInheritedChild.class.getAnnotations()[0].annotationType());

        Assert.assertEquals(0, AddedInheritedChild.class.getDeclaredAnnotations().length);
    }

    /**
     * test the annotation inheritence works the way it is supposed to if a
     * superclass
     * has an inherited method added
     */
    @Test
    public void testRemovedInheritedAnnotationBehavoir() {
        Assert.assertFalse(RemovedInheritedChild.class.isAnnotationPresent(InheritedAnnotation.class));
        Assert.assertFalse(RemovedInheritedChild.class.isAnnotationPresent(NotInheritedAnnotation.class));

        Assert.assertNull(RemovedInheritedChild.class.getAnnotation(InheritedAnnotation.class));

        Assert.assertEquals(0, RemovedInheritedChild.class.getAnnotations().length);

        Assert.assertEquals(0, RemovedInheritedChild.class.getDeclaredAnnotations().length);
    }

}
