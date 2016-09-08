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

package a.org.fakereplace.test.replacement.repeat;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RepeatReplacementTest {

    @Test
    public void multipleReplacementTest() throws SecurityException, NoSuchFieldException, NoSuchMethodException {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(Replace.class, Replace1.class);
        r.replaceQueuedClasses();

        Replace.class.getDeclaredField("field1");
        Replace.class.getDeclaredField("sfield1");
        Replace.class.getDeclaredMethod("method1");
        Replace.class.getDeclaredMethod("smethod1");

        try {
            Replace.class.getDeclaredField("field");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredField("sfield");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredMethod("method");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
        try {
            Replace.class.getDeclaredMethod("smethod");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }

        r = new ClassReplacer();
        r.queueClassForReplacement(Replace.class, Replace2.class);
        r.replaceQueuedClasses();

        Replace.class.getDeclaredField("field2");
        Replace.class.getDeclaredField("sfield2");
        Replace.class.getDeclaredMethod("method2");
        Replace.class.getDeclaredMethod("smethod2");

        try {
            Replace.class.getDeclaredField("field1");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredField("sfield1");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredMethod("method1");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
        try {
            Replace.class.getDeclaredMethod("smethod1");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
        r = new ClassReplacer();
        r.queueClassForReplacement(Replace.class, Replace3.class);
        r.replaceQueuedClasses();

        Replace.class.getDeclaredField("field3");
        Replace.class.getDeclaredField("sfield3");
        Replace.class.getDeclaredMethod("method3");
        Replace.class.getDeclaredMethod("smethod3");

        try {
            Replace.class.getDeclaredField("field2");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredField("sfield2");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredMethod("method2");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
        try {
            Replace.class.getDeclaredMethod("smethod2");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
        r = new ClassReplacer();
        r.queueClassForReplacement(Replace.class, Replace4.class);
        r.replaceQueuedClasses();

        Replace.class.getDeclaredField("field4");
        Replace.class.getDeclaredField("sfield4");
        Replace.class.getDeclaredMethod("method4");
        Replace.class.getDeclaredMethod("smethod4");

        try {
            Replace.class.getDeclaredField("field3");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredField("sfield3");
            Assert.fail();
        } catch (NoSuchFieldException e) {
        }
        try {
            Replace.class.getDeclaredMethod("method3");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
        try {
            Replace.class.getDeclaredMethod("smethod3");
            Assert.fail();
        } catch (NoSuchMethodException e) {
        }
    }

}
