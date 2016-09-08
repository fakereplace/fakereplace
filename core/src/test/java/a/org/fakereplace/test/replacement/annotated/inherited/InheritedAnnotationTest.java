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
