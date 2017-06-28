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

package a.org.fakereplace.test.replacement.instancefield;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

public class InstanceFieldTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(InstanceFieldClass.class, InstanceFieldClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test
    public void testAddingInstanceField() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        InstanceFieldClass ns = new InstanceFieldClass();
        ns.inc();
        Assert.assertEquals(1, ns.get());
        ns.inclong();
        Assert.assertEquals(2, ns.getlong());
        Assert.assertEquals("aa", ns.getSv());
    }

    @Test
    public void testChangingInstanceFieldType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ChangeFieldType.class, ChangeFieldType1.class);
        rep.replaceQueuedClasses();
        ChangeFieldType type = new ChangeFieldType();
        Assert.assertEquals(20, type.getValue());
    }

    @Test
    public void testSettingObjectInstanceField() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        InstanceFieldClass ns = new InstanceFieldClass();
        ns.setFa2(this);
        Assert.assertEquals(this, ns.getFa2());
    }

    @Test
    public void testReplacementOrder() {
        ReaderClass readerClass = new ReaderClass();
        Assert.assertEquals(-1, readerClass.readField());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ReaderClass.class, ReaderClass1.class);
        rep.rewriteNames(FieldClass.class, FieldClass1.class);
        rep.replaceQueuedClasses();
        Assert.assertEquals(0, readerClass.readField()); //by rights this should throw a NoSuchFieldError, but the test for this would be kinda slow, and I don't think it really hurts
        Assert.assertEquals(0, readerClass.readStaticField());

        rep.queueClassForReplacement(FieldClass.class, FieldClass1.class);
        rep.replaceQueuedClasses();

        Assert.assertEquals(0, readerClass.readField());
        Assert.assertEquals(0, readerClass.readStaticField());
        readerClass.writeField(1);
        readerClass.writeStaticField(1);
        Assert.assertEquals(1, readerClass.readField());
        Assert.assertEquals(1, readerClass.readStaticField());

    }
}
