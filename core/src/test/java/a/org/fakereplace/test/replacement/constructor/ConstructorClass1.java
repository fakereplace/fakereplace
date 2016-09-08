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

package a.org.fakereplace.test.replacement.constructor;

import java.util.List;
import java.util.Set;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class ConstructorClass1 {

    public ConstructorClass1(String a) {
        value = a;
    }

    private ConstructorClass1(List<String> a) {
        value = "h";
    }

    private ConstructorClass1(Set<String> a) {
        value = "e";
    }

    public ConstructorClass1(int i1, int i2, int i3, int i4, int i5, int i6) {
        value = "h";
    }

    String value = "a";

    public String getValue() {
        return value;
    }
}
