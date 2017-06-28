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

package org.fakereplace.data;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * This class is responsible for tracking instances of certain classes as they
 * are loaded
 *
 * @author stuart
 */
public class InstanceTracker {

    private static ConcurrentMap<String, Set<Object>> data = new ConcurrentHashMap<String, Set<Object>>();

    public static void add(String type, Object object) {
        Set<Object> set = data.get(type);
        if(set == null) {
            set = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));
            Set<Object> existing = data.putIfAbsent(type, set);
            if(existing != null) {
                set = existing;
            }
        }
        set.add(object);
    }

    public static Set<?> get(String type) {
        final Set<Object> result =  data.get(type);
        if(result != null) {
            return result;
        }
        return Collections.emptySet();
    }
}
