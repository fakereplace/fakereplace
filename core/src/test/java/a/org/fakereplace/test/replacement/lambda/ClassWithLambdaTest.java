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
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore("This still needs some work, we need to rewrite the BootstrapMethods attribute")
public class ClassWithLambdaTest {
    @Test
    public void givenStaticMethodWithLambda_whenLambdaBodyChange_thenItsProperlyReplaced() {
        assertEquals("first", LambdaClass.getMessageProducer().get());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(LambdaClass.class, LambdaClassM.class);
        rep.replaceQueuedClasses();
        assertEquals("second", LambdaClass.getMessageProducer().get());
    }

    @Test
    public void givenInstanceMethodWithLambda_whenLambdaBodyChange_thenItsProperlyReplaced() {
        LambdaInstanceClass lambdaInstanceClass = new LambdaInstanceClass();
        assertEquals("first", lambdaInstanceClass.getMessageProducer().get());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(LambdaInstanceClass.class, LambdaInstanceClassM.class);
        rep.replaceQueuedClasses();
        assertEquals("second", lambdaInstanceClass.getMessageProducer().get());
    }

    @Test
    public void givenNewMethodWithLambda_whenNewMethodWithLambda_thenItsProperlyReplaced() throws Exception {
        LambdaInstanceClassNewMethod lambdaInstanceClass = new LambdaInstanceClassNewMethod();
        assertEquals("first", lambdaInstanceClass.getMessageProducer().get());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(LambdaInstanceClassNewMethod.class, LambdaInstanceClassNewMethodM.class);
        rep.replaceQueuedClasses();

        Method origMethodModified = lambdaInstanceClass.getClass().getDeclaredMethod("getMessageProducer");
        Object origMethodModifiedResult = origMethodModified.invoke(lambdaInstanceClass);
        assertEquals("third orig", ((Supplier) origMethodModifiedResult).get());

        Method newMethod = lambdaInstanceClass.getClass().getDeclaredMethod("getAnotherMessageProducer");
        Object newMethodResult = newMethod.invoke(lambdaInstanceClass);
        assertEquals("third new", ((Supplier) newMethodResult).get());
    }

    @Test
    public void whenNewLambda_thenProperlyReplaced() throws InterruptedException {
        LambdaInstanceClassNewLambda lambdaInstanceClass = new LambdaInstanceClassNewLambda();
        assertEquals("first", lambdaInstanceClass.getMessageProducer().get());

        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(LambdaInstanceClassNewLambda.class, LambdaInstanceClassNewLambdaM.class);
        rep.replaceQueuedClasses();

        assertEquals("fourth", lambdaInstanceClass.getMessageProducer().get());
    }
}
