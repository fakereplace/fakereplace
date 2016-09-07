/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.runtime;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.collect.MapMaker;

/**
 * Maps a unique field signature to an arbitrary number. This number will be the same for all fields with the
 * same name and type, this field number is used to actually store the runtime data when adding fields
 *
 * @author stuart
 */
public class FieldReferenceDataStore {

    private static final FieldReferenceDataStore INSTANCE = new FieldReferenceDataStore();

    private final AtomicInteger counter = new AtomicInteger();

    private final Map<FieldReference, Integer> addedFieldNumbers = new MapMaker().makeComputingMap(new Function<FieldReference, Integer>() {
        public Integer apply(FieldReference from) {
            return counter.incrementAndGet();
        }
    });

    private FieldReferenceDataStore() {

    }

    public Integer getFieldNo(String fieldName, String desc) {
        return addedFieldNumbers.get(new FieldReference(fieldName, desc));
    }

    public static FieldReferenceDataStore instance() {
        return INSTANCE;
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
