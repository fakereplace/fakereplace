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

package a.org.fakereplace.test.replacement.privatemethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PrivateMethodTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(PrivateMethodClass.class, PrivateMethodClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test
    public void testAddingPrivateMethod() {
        PrivateMethodClass instance = new PrivateMethodClass();
        Assert.assertEquals(1, instance.getResult());
    }

    @Test
    public void testAddingPrivateMethodByReflection() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = PrivateMethodClass.class.getDeclaredMethod("realResult");
        method.setAccessible(true);
        PrivateMethodClass cls = new PrivateMethodClass();
        Assert.assertEquals(1, method.invoke(cls));
    }

    @Test(expected = IllegalAccessException.class)
    public void testExceptionIfNotSetAccessible() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = PrivateMethodClass.class.getDeclaredMethod("realResult");
        PrivateMethodClass cls = new PrivateMethodClass();
        Assert.assertEquals(1, method.invoke(cls));
    }
}
