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

package a.org.fakereplace.test.replacement.finalmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class FinalMethodReplacementTest {

    @BeforeClass
    public static void setup() {
        ClassReplacer cr = new ClassReplacer();
        cr.queueClassForReplacement(FinalMethodClass.class, FinalMethodClass1.class);
        cr.replaceQueuedClasses();
    }

    @Ignore
    @Test
    public void testNonFinalMethodIsNonFinal() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        FinalMethodClass cl = new FinalMethodClass();
        Method method = cl.getClass().getMethod("finalMethod-replaced");
        Assert.assertTrue(Modifier.isFinal(method.getModifiers()));
        Assert.assertEquals("finalMethod-replaced", method.invoke(cl));
    }

    @Ignore
    @Test
    public void testFinalMethodIsFinal() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        FinalMethodClass cl = new FinalMethodClass();
        Method method = cl.getClass().getMethod("nonFinalMethod-replaced");
        Assert.assertFalse(Modifier.isFinal(method.getModifiers()));
        Assert.assertEquals("nonFinalMethod-replaced", method.invoke(cl));
    }
}
