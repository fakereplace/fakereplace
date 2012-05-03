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
import java.util.Set;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * A {@code Multimap} that cannot hold duplicate key-value pairs. Adding a
 * key-value pair that's already in the multimap has no effect.
 * <p/>
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link Set} of values, while {@link #entries} returns a {@code
 * Set} of map entries. Though the method signature doesn't say so explicitly,
 * the map returned by {@link #asMap} has {@code Set} values.
 *
 * @author Jared Levy
 */
@GwtCompatible
public interface SetMultimap<K, V> extends Multimap<K, V> {
    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because a {@code SetMultimap} has unique values for a given key, this
     * method returns a {@link Set}, instead of the {@link java.util.Collection}
     * specified in the {@link Multimap} interface.
     */
    Set<V> get(K key);

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because a {@code SetMultimap} has unique values for a given key, this
     * method returns a {@link Set}, instead of the {@link java.util.Collection}
     * specified in the {@link Multimap} interface.
     */
    Set<V> removeAll(Object key);

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because a {@code SetMultimap} has unique values for a given key, this
     * method returns a {@link Set}, instead of the {@link java.util.Collection}
     * specified in the {@link Multimap} interface.
     * <p/>
     * <p>Any duplicates in {@code values} will be stored in the multimap once.
     */
    Set<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Because a {@code SetMultimap} has unique values for a given key, this
     * method returns a {@link Set}, instead of the {@link java.util.Collection}
     * specified in the {@link Multimap} interface.
     */
    Set<Map.Entry<K, V>> entries();

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Though the method signature doesn't say so explicitly, the returned map
     * has {@link Set} values.
     */
    Map<K, Collection<V>> asMap();

    /**
     * Compares the specified object to this multimap for equality.
     * <p/>
     * <p>Two {@code SetMultimap} instances are equal if, for each key, they
     * contain the same values. Equality does not depend on the ordering of keys
     * or values.
     */
    boolean equals(Object obj);
}
