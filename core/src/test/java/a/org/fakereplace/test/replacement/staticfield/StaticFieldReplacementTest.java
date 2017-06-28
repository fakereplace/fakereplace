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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class StaticFieldReplacementTest {

    @BeforeClass
    public static void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticFieldClass.class, StaticFieldClass1.class);
        r.replaceQueuedClasses(true);
    }

    @Test
    public void testStaticFieldReplacement() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Long v = StaticFieldClass.incAndGet();
        Assert.assertEquals((Object)1L, v);
        v = StaticFieldClass.incAndGet();
        Assert.assertEquals((Object)2L, v);
    }

    @Test
    public void testAddedStaticFieldGetDeclaredFields() {
        Field[] fields = StaticFieldClass.class.getDeclaredFields();
        boolean removedField = false;
        boolean longField = false;
        boolean list = false;
        for (Field f : fields) {
            if (f.getName().equals("removedField")) {
                removedField = true;
            }
            if (f.getName().equals("longField")) {
                longField = true;
            }
            if (f.getName().equals("list")) {
                list = true;
            }
        }
        Assert.assertTrue( list);
        Assert.assertTrue( longField);
        Assert.assertTrue( !removedField);
    }

    @Test
    public void testAddedStaticFieldGetFields() {
        Field[] fields = StaticFieldClass.class.getFields();
        boolean removedField = false;
        boolean longField = false;
        boolean list = false;
        for (Field f : fields) {
            if (f.getName().equals("removedField")) {
                removedField = true;
            }
            if (f.getName().equals("longField")) {
                longField = true;
            }
            if (f.getName().equals("list")) {
                list = true;
            }
        }
        Assert.assertTrue( !list);
        Assert.assertTrue( longField);
        Assert.assertTrue( !removedField);
    }

    @Test
    public void testStaticFieldGenericType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Field f = StaticFieldClass.class.getDeclaredField("list");
        Assert.assertEquals(String.class,  ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]);
    }

    @Test(expected = NoSuchFieldException.class)
    public void testStaticFieldGetFieldNonPublicFieldsNotAccessible() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Field f = StaticFieldClass.class.getField("list");
    }

}
