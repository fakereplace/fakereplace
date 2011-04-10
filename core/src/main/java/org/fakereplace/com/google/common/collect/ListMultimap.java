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

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * A {@code Multimap} that can hold duplicate key-value pairs and that maintains
 * the insertion ordering of values for a given key.
 * <p/>
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link List} of values. Though the method signature doesn't say
 * so explicitly, the map returned by {@link #asMap} has {@code List} values.
 *
 * @author Jared Levy
 */
@GwtCompatible
public interface ListMultimap<K, V> extends Multimap<K, V> {
    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because the values for a given key may have duplicates and follow the
     * insertion ordering, this method returns a {@link List}, instead of the
     * {@link java.util.Collection} specified in the {@link Multimap} interface.
     */
    List<V> get(K key);

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because the values for a given key may have duplicates and follow the
     * insertion ordering, this method returns a {@link List}, instead of the
     * {@link java.util.Collection} specified in the {@link Multimap} interface.
     */
    List<V> removeAll(Object key);

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because the values for a given key may have duplicates and follow the
     * insertion ordering, this method returns a {@link List}, instead of the
     * {@link java.util.Collection} specified in the {@link Multimap} interface.
     */
    List<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Though the method signature doesn't say so explicitly, the returned map
     * has {@link List} values.
     */
    Map<K, Collection<V>> asMap();

    /**
     * Compares the specified object to this multimap for equality.
     * <p/>
     * <p>Two {@code ListMultimap} instances are equal if, for each key, they
     * contain the same values in the same order. If the value orderings disagree,
     * the multimaps will not be considered equal.
     */
    boolean equals(Object obj);
}
