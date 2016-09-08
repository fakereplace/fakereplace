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

package a.org.fakereplace.test.replacement.lambda;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("This still needs some work, we need to rewrite the BootstrapMethods attribute")
public class ClassWithLambdaTest {
    @Test
    public void testReplaceClassWithLambda() throws Exception {

        Assert.assertEquals("first", LambdaClass.getMessageProducer().call());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(LambdaClass.class, LambdaClass1.class);
        rep.replaceQueuedClasses();
        Assert.assertEquals("second", LambdaClass.getMessageProducer().call());
    }

}
