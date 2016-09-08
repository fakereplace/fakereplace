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

package org.fakereplace.reflection;

import org.fakereplace.runtime.FieldDataStore;

/**
 * Class that knows how to set and get replaced fields. This is very inefficent,
 * and should be replaced with bytecode generation based reflection
 *
 * @author stuart
 */
public class FieldAccessor {

    private final Class<?> declaringClass;
    private final Integer mapKey;
    private final boolean staticField;

    public FieldAccessor(Class<?> declaringClass, int mapKey, boolean staticField) {
        this.declaringClass = declaringClass;
        this.mapKey = mapKey;
        this.staticField = staticField;
    }

    public void set(Object object, Object value) throws IllegalAccessException {
        if(staticField) {
            FieldDataStore.setValue(declaringClass, value, mapKey);
        } else {
            FieldDataStore.setValue(object, value, mapKey);
        }
    }

    public Object get(Object object) throws IllegalAccessException {
        if(staticField) {
            return FieldDataStore.getValue(declaringClass, mapKey);
        } else {
            return FieldDataStore.getValue(object, mapKey);
        }
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }
}
