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

package org.fakereplace.test.replacement.staticfield.repeat;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class StaticFieldRepeatReplacementTest {
    @Test
    public void firstReplacement() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticFieldRepeatClass.class, StaticFieldRepeatClass1.class);
        r.replaceQueuedClasses();

        Field someField = StaticFieldRepeatClass.class.getDeclaredField("someField");
        someField.setAccessible(true);
        someField.set(null, 10);
        Field otherField = StaticFieldRepeatClass.class.getDeclaredField("otherField");
        otherField.set(null, this);
        Field removedField = StaticFieldRepeatClass.class.getDeclaredField("removedField");
    }

    @Test(dependsOnMethods = "firstReplacement")
    public void secondReplacement() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticFieldRepeatClass.class, StaticFieldRepeatClass2.class);
        r.replaceQueuedClasses();

        Field someField = StaticFieldRepeatClass.class.getDeclaredField("someField");
        someField.setAccessible(true);
        assert someField.get(null).equals(10);
        Field otherField = StaticFieldRepeatClass.class.getDeclaredField("otherField");
        otherField.setAccessible(true);
        assert otherField.get(null) == this;
        try {
            Field removedField = StaticFieldRepeatClass.class.getDeclaredField("removedField");
            assert false : "Field should have been removed";
        } catch (NoSuchFieldException e) {

        }
    }

}
