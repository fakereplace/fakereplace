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

package a.org.fakereplace.test.replacement.annotated.field;

import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class AnnotatedFieldTest {

    @Test
    public void testFieldAnnotations() throws SecurityException, NoSuchFieldException {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(FieldAnnotated.class, FieldAnnotated1.class);
        r.replaceQueuedClasses();

        Field m1 = FieldAnnotated.class.getField("field1");
        Field m2 = FieldAnnotated.class.getField("field2");
        Field m3 = FieldAnnotated.class.getField("field3");
        assert m1.getAnnotation(FieldAnnotation.class).value().equals("1");
        assert !m2.isAnnotationPresent(FieldAnnotation.class);
        assert m3.getAnnotation(FieldAnnotation.class).value().equals("3");

    }

}
