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

package a.org.fakereplace.test.replacement.annotated.method;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class AnnotatedMethodParametersTest {

    @BeforeClass
    public static void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(MethodParameterAnnotated.class, MethodParameterAnnotated1.class);
        r.replaceQueuedClasses();

    }

    @Test
    public void testMethodParameterAnnotations() throws SecurityException, NoSuchMethodException {

        Method m1 = MethodParameterAnnotated.class.getMethod("method1", int.class);
        Method m2 = MethodParameterAnnotated.class.getMethod("method2", int.class);
        Method m3 = MethodParameterAnnotated.class.getMethod("method3", int.class);

        Assert.assertEquals("1", ((MethodAnnotation) m1.getParameterAnnotations()[0][0]).value());
        Assert.assertEquals(0, m2.getParameterAnnotations()[0].length);
        Assert.assertEquals("3", ((MethodAnnotation) m3.getParameterAnnotations()[0][0]).value());

    }

}
