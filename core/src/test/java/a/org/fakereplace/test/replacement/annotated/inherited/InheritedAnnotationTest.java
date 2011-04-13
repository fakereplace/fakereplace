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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InheritedAnnotationTest {

    @BeforeClass
    public void setup() {
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
        assert InheritChild.class.isAnnotationPresent(InheritedAnnotation.class);
        assert !InheritChild.class.isAnnotationPresent(NotInheritedAnnotation.class);

        assert InheritChild.class.getAnnotation(InheritedAnnotation.class).value() == 10;

        assert InheritChild.class.getAnnotations().length == 1;
        assert InheritChild.class.getAnnotations()[0].annotationType() == InheritedAnnotation.class;

        assert InheritChild.class.getDeclaredAnnotations().length == 0;
    }

    /**
     * test the annotation inheritence works the way it is supposed to if a
     * superclass
     * has an inherited method added
     */
    @Test
    public void testAddedInheritedAnnotationBehavoir() {
        assert AddedInheritedChild.class.isAnnotationPresent(InheritedAnnotation.class);
        assert !AddedInheritedChild.class.isAnnotationPresent(NotInheritedAnnotation.class);

        assert AddedInheritedChild.class.getAnnotation(InheritedAnnotation.class).value() == 20;

        assert AddedInheritedChild.class.getAnnotations().length == 1;
        assert AddedInheritedChild.class.getAnnotations()[0].annotationType() == InheritedAnnotation.class;

        assert AddedInheritedChild.class.getDeclaredAnnotations().length == 0;
    }

    /**
     * test the annotation inheritence works the way it is supposed to if a
     * superclass
     * has an inherited method added
     */
    @Test
    public void testRemovedInheritedAnnotationBehavoir() {
        assert !RemovedInheritedChild.class.isAnnotationPresent(InheritedAnnotation.class);
        assert !RemovedInheritedChild.class.isAnnotationPresent(NotInheritedAnnotation.class);

        assert RemovedInheritedChild.class.getAnnotation(InheritedAnnotation.class) == null;

        assert RemovedInheritedChild.class.getAnnotations().length == 0;

        assert RemovedInheritedChild.class.getDeclaredAnnotations().length == 0;
    }

}
