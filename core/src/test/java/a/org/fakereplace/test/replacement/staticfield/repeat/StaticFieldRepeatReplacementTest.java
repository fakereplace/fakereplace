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

package a.org.fakereplace.test.replacement.staticfield.repeat;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

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

        r = new ClassReplacer();
        r.queueClassForReplacement(StaticFieldRepeatClass.class, StaticFieldRepeatClass2.class);
        r.replaceQueuedClasses();

        someField = StaticFieldRepeatClass.class.getDeclaredField("someField");
        someField.setAccessible(true);
        Assert.assertEquals(10, someField.get(null));
        otherField = StaticFieldRepeatClass.class.getDeclaredField("otherField");
        otherField.setAccessible(true);
        Assert.assertEquals(this,  otherField.get(null));
        try {
            removedField = StaticFieldRepeatClass.class.getDeclaredField("removedField");
            Assert.fail();
        } catch (NoSuchFieldException e) {

        }
    }

}
