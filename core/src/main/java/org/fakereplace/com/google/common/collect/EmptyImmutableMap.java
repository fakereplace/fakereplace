/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.com.google.common.collect;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

import java.util.Map;


/**
 * An empty immutable map.
 *
 * @author Jesse Wilson
 * @author Kevin Bourrillion
 */
@GwtCompatible(serializable = true)
final class EmptyImmutableMap extends ImmutableMap<Object, Object> {
    static final EmptyImmutableMap INSTANCE = new EmptyImmutableMap();

    private EmptyImmutableMap() {
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public ImmutableSet<Entry<Object, Object>> entrySet() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Object> keySet() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableCollection<Object> values() {
        return ImmutableCollection.EMPTY_IMMUTABLE_COLLECTION;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Map) {
            Map<?, ?> that = (Map<?, ?>) object;
            return that.isEmpty();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "{}";
    }

    Object readResolve() {
        return INSTANCE; // preserve singleton property
    }

    private static final long serialVersionUID = 0;
}
