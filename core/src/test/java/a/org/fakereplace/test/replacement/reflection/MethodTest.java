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

package a.org.fakereplace.test.replacement.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fakereplace.core.Constants;
import org.fakereplace.util.NoInstrument;
import org.junit.Test;
import junit.framework.Assert;

public class MethodTest {
    @Test
    public void testDelegatorMethodAdded() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IllegalAccessException {
        TestRunner.runTest();
    }

    @Test
    public void testGetDeclaredMethods() {
        DoStuff d = new DoStuff();
        Method[] meths = d.getClass().getDeclaredMethods();
        for (Method m : meths) {
            if (m.getName().equals(Constants.ADDED_METHOD_NAME) || m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME)) {
                throw new RuntimeException("Added method delegator showing up in declared methods");
            }
        }
    }

    @NoInstrument
    private static class TestRunner {
        public static void runTest() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            DoStuff d = new DoStuff();
            Method m = d.getClass().getMethod(Constants.ADDED_METHOD_NAME, int.class, Object[].class);
            try {
                m.invoke(d, 10, null);
                Assert.fail();
            } catch (InvocationTargetException expected) {
                Assert.assertEquals(NoSuchMethodError.class, expected.getCause().getClass());
            }
        }
    }

}
