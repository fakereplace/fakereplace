/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.fakereplace.com.google.common.collect;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * Basic implementation of the {@link SortedSetMultimap} interface. It's a
 * wrapper around {@link AbstractMultimap} that converts the returned
 * collections into sorted sets. The {@link #createCollection} method
 * must return a {@code SortedSet}.
 *
 * @author Jared Levy
 */
@GwtCompatible
abstract class AbstractSortedSetMultimap<K, V>
        extends AbstractSetMultimap<K, V> implements SortedSetMultimap<K, V> {
    /**
     * Creates a new multimap that uses the provided map.
     *
     * @param map place to store the mapping from each key to its corresponding
     *            values
     */
    protected AbstractSortedSetMultimap(Map<K, Collection<V>> map) {
        super(map);
    }

    @Override
    abstract SortedSet<V> createCollection();

    @Override
    public SortedSet<V> get(K key) {
        return (SortedSet<V>) super.get(key);
    }

    @Override
    public SortedSet<V> removeAll(Object key) {
        return (SortedSet<V>) super.removeAll(key);
    }

    @Override
    public SortedSet<V> replaceValues(
            K key, Iterable<? extends V> values) {
        return (SortedSet<V>) super.replaceValues(key, values);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Consequently, the values do not follow their natural ordering or the
     * ordering of the value comparator.
     */
    @Override
    public Collection<V> values() {
        return super.values();
    }

    private static final long serialVersionUID = 430848587173315748L;
}
