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

package a.org.fakereplace.test.replacement.staticfield;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

/**
 * when changing instance fields to static existing reference will still
 * reference the instance field
 *
 * @author stuart
 */
public class StaticToInstanceTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticToInstance.class, StatictoInstance1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testStaticToInstance() {
        StaticToInstance f1 = new StaticToInstance();
        StaticToInstance f2 = new StaticToInstance();
        f1.setField(100);
        Assert.assertEquals(20,  f2.getField());
    }

    @Test
    public void testStaticToInstanceViaReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        StaticToInstance f1 = new StaticToInstance();
        StaticToInstance f2 = new StaticToInstance();
        Field f = f1.getClass().getDeclaredField("field");
        f.setAccessible(true);
        f.setInt(f1, 200);
        Assert.assertEquals(20,  f.getInt(f2));
    }

}
