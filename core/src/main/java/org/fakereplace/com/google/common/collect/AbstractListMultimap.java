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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * Basic implementation of the {@link ListMultimap} interface. It's a wrapper
 * around {@link AbstractMultimap} that converts the returned collections into
 * {@code Lists}. The {@link #createCollection} method must return a {@code
 * List}.
 *
 * @author Jared Levy
 */
@GwtCompatible
abstract class AbstractListMultimap<K, V>
        extends AbstractMultimap<K, V> implements ListMultimap<K, V> {
    /**
     * Creates a new multimap that uses the provided map.
     *
     * @param map place to store the mapping from each key to its corresponding
     *            values
     */
    protected AbstractListMultimap(Map<K, Collection<V>> map) {
        super(map);
    }

    @Override
    abstract List<V> createCollection();

    @Override
    public List<V> get(K key) {
        return (List<V>) super.get(key);
    }

    @Override
    public List<V> removeAll(Object key) {
        return (List<V>) super.removeAll(key);
    }

    @Override
    public List<V> replaceValues(
            K key, Iterable<? extends V> values) {
        return (List<V>) super.replaceValues(key, values);
    }

    /**
     * Stores a key-value pair in the multimap.
     *
     * @param key   key to store in the multimap
     * @param value value to store in the multimap
     * @return {@code true} always
     */
    @Override
    public boolean put(K key, V value) {
        return super.put(key, value);
    }

    /**
     * Compares the specified object to this multimap for equality.
     * <p/>
     * <p>Two {@code ListMultimap} instances are equal if, for each key, they
     * contain the same values in the same order. If the value orderings disagree,
     * the multimaps will not be considered equal.
     */
    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private static final long serialVersionUID = 6588350623831699109L;
}
