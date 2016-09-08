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

package a.org.fakereplace.test.replacement.abstractmethod;

import java.lang.reflect.InvocationTargetException;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractMethodTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(AbstractClass.class, AbstractClass1.class);
        rep.queueClassForReplacement(AbstractCaller.class, AbstractCaller1.class);
        rep.queueClassForReplacement(BigChild.class, BigChild1.class);
        rep.queueClassForReplacement(SmallChild.class, SmallChild1.class);
        rep.replaceQueuedClasses();
    }

    @Test
    public void testAbstractMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        AbstractCaller caller = new AbstractCaller();
        BigChild big = new BigChild();
        SmallChild small = new SmallChild();

        Assert.assertEquals("big", caller.getValue(big));
        Assert.assertEquals("small", caller.getValue(small));
    }

}
