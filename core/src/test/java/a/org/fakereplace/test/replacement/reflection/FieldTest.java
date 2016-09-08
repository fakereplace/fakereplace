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

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

public class FieldTest {
    @Test
    public void testFieldAccess() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        DoStuff d = new DoStuff();
        Field f = d.getClass().getField("field");
        String s = (String) f.get(d);
        Assert.assertEquals("hello world", s);
        f.set(d, "bye world");
        s = (String) f.get(d);
        Assert.assertEquals("bye world", s);

    }

}
