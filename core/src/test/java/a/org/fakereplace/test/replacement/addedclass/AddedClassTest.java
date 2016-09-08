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

package a.org.fakereplace.test.replacement.addedclass;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.Test;

public class AddedClassTest {

    @Test
    public void testAddedClass() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(ReplacedClass.class, ReplacedClass1.class);
        r.addNewClass(AddedClass1.class, "a.org.fakereplace.test.replacement.addedclass.AddedClass");
        r.replaceQueuedClasses();

        ReplacedClass c = new ReplacedClass();
        Assert.assertEquals("hello Bob", c.getValue());

    }
}
