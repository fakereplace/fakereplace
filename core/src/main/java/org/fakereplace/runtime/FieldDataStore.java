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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.fakereplace.util.NullSafeConcurrentHashMap;

/**
 * This class holds field data for added fields. It maintains a weakly
 * referenced computing map of instance to field value.
 *
 * @author Stuart Douglas
 */
public class FieldDataStore {
    private static final Map<Object, Map<Integer, Object>> fieldData = Collections.synchronizedMap(new WeakHashMap<>());

    public static Object getValue(Object instance, int field) {
        Map<Integer, Object> map = fieldData.get(instance);
        if(map != null) {
            Object ret = map.get(field);
            if(ret != null) {
                return ret;
            }
        }
        String type = FieldReferenceDataStore.instance().getFieldDescriptor(field);
        if(type.length() == 1) {
            return 0;
        } else {
            return null;
        }
    }

    public static void setValue(Object instance, Object value, int field) {
        fieldData.computeIfAbsent(instance, (e) -> new NullSafeConcurrentHashMap<>()).put(field, value);
    }
}
