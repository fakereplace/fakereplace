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

package a.org.fakereplace.test.replacement.staticmethod.visibility;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import a.org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass;
import a.org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass1;
import a.org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.UnchangedStaticMethodCallingClass;
import a.org.fakereplace.test.util.ClassReplacer;

public class IncreaseVisibilityTest {
    @BeforeClass
    public static void setup() throws InterruptedException {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticMethodVisibilityCallingClass.class, StaticMethodVisibilityCallingClass1.class);
        r.queueClassForReplacement(StaticMethodVisibilityClass.class, StaticMethodVisibilityClass1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testExistingMethod() {
        Assert.assertEquals("hello world", StaticMethodVisibilityClass.callingMethod());
    }

    @Test
    public void testNewExternalMethod() {
        Assert.assertEquals("hello world", StaticMethodVisibilityCallingClass.callingClass());
    }

    @Test
    public void testUnchangedClassCallingExternalMethod() {
        Assert.assertEquals("hello world", UnchangedStaticMethodCallingClass.callingClass());
    }
}
