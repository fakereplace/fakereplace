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

package a.org.fakereplace.test.replacement.optionaldep;

import org.junit.Assert;
import org.junit.Test;
import a.org.fakereplace.test.util.ClassReplacer;

/**
 * @author Stuart Douglas
 */
public class OptionalDependencyTest {

    @Test
    public void testOptionalDependencyInRewrite() {
        OptionalDep1 instance = new OptionalDep1();
        Assert.assertEquals("hello", instance.sayHello());

        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(OptionalDep1.class, OptionalDep2.class);
        rep.rewriteNames("com.notfound.Missing", HelloSayer.class.getName());
        rep.replaceQueuedClasses();

        try {
            instance.sayHello();
            Assert.fail();
        } catch (NoClassDefFoundError e) {

        }

    }

}
