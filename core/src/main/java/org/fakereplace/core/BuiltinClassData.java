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

package org.fakereplace.core;

public class BuiltinClassData {

    private static final String[] doNotInstrument =
            {"org/fakereplace", "java/math", "java/lang", "java/util/concurrent", "java/util/Currency", "java/util/Random", "java/util",};

    private static final String[] exceptions = {"java/lang/reflect/Proxy",};

    public static boolean skipInstrumentation(String className) {
        if (className == null) {
            return true;
        }
        className = className.replace('.', '/');
        for (String s : exceptions) {
            if (className.startsWith(s)) {
                return false;
            }
        }
        for (String s : doNotInstrument) {
            if (className.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
