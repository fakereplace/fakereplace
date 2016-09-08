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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps a unique field signature to an arbitrary number. This number will be the same for all fields with the
 * same name and type, this field number is used to actually store the runtime data when adding fields
 *
 * @author stuart
 */
public class FieldReferenceDataStore {

    private static final FieldReferenceDataStore INSTANCE = new FieldReferenceDataStore();

    private int counter = 0;

    private final Map<FieldReference, Integer> addedFieldNumbers = new ConcurrentHashMap<>();
    private final Map<Integer, FieldReference> fieldsByNumber = new ConcurrentHashMap<>();

    private FieldReferenceDataStore() {

    }

    public synchronized Integer getFieldNo(String fieldName, String desc) {
        FieldReference ref = new FieldReference(fieldName, desc);
        Integer existing = addedFieldNumbers.get(ref);
        if (existing != null) {
            return existing;
        }
        int ret = counter++;
        addedFieldNumbers.put(ref, ret);
        fieldsByNumber.put(ret, ref);
        return ret;
    }

    public static FieldReferenceDataStore instance() {
        return INSTANCE;
    }

    public String getFieldDescriptor(int field) {
        FieldReference instance = fieldsByNumber.get(field);
        if(instance == null) {
            return null;
        }
        return instance.descriptor;
    }

    private static class FieldReference {
        private final String name;
        private final String descriptor;

        public FieldReference(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FieldReference other = (FieldReference) obj;
            if (descriptor == null) {
                if (other.descriptor != null)
                    return false;
            } else if (!descriptor.equals(other.descriptor))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

    }

}
