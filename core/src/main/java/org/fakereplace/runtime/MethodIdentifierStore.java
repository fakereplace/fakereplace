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

package org.fakereplace.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns a method number for a generated method. Methods with the same name
 * and descriptor are assigned the same number to make emulating virtual calls
 * easier. The redifined method can call super.REDEFINED_METHOD with the same
 * method number and if the method exists on the superclass then it is handled
 * automatically
 *
 * @author Stuart Douglas
 */
public class MethodIdentifierStore {

    private static final MethodIdentifierStore INSTANCE = new MethodIdentifierStore();

    private final Map<String, Map<String, Integer>> data = new HashMap<>();

    private int methodNo = 0;

    private MethodIdentifierStore() {

    }

    public synchronized int getMethodNumber(String name, String descriptor) {
        if (!data.containsKey(name)) {
            data.put(name, new HashMap<>());
        }
        Map<String, Integer> im = data.get(name);
        if (!im.containsKey(descriptor)) {
            im.put(descriptor, methodNo++);
        }
        return im.get(descriptor);
    }

    public static MethodIdentifierStore instance() {
        return INSTANCE;
    }

}
