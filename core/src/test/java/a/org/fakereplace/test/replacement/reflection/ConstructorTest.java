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

import java.lang.reflect.Constructor;

import org.fakereplace.core.ConstructorArgument;
import org.junit.Test;

public class ConstructorTest {
    @Test
    public void testGetDeclaredConstrcutors() {

        DoStuff d = new DoStuff();
        Constructor<?>[] meths = d.getClass().getDeclaredConstructors();
        for (Constructor<?> m : meths) {
            if (m.getParameterTypes().length == 3 && m.getParameterTypes()[2].equals(ConstructorArgument.class)) {
                throw new RuntimeException("Added constructor delegator showing up in declared methods");
            }
        }
    }

}
