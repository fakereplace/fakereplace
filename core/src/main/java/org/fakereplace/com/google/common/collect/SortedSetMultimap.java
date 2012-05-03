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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * A {@code SetMultimap} whose set of values for a given key are kept sorted;
 * that is, they comprise a {@link SortedSet}. It cannot hold duplicate
 * key-value pairs; adding a key-value pair that's already in the multimap has
 * no effect. This interface does not specify the ordering of the multimap's
 * keys.
 * <p/>
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link SortedSet} of values, while {@link Multimap#entries()}
 * returns a {@link Set} of map entries. Though the method signature doesn't say
 * so explicitly, the map returned by {@link #asMap} has {@code SortedSet}
 * values.
 *
 * @author Jared Levy
 */
@GwtCompatible
public interface SortedSetMultimap<K, V> extends SetMultimap<K, V> {
    /**
     * Returns a collection view of all values associated with a key. If no
     * mappings in the multimap have the provided key, an empty collection is
     * returned.
     * <p/>
     * <p>Changes to the returned collection will update the underlying multimap,
     * and vice versa.
     * <p/>
     * <p>Because a {@code SortedSetMultimap} has unique sorted values for a given
     * key, this method returns a {@link SortedSet}, instead of the
     * {@link java.util.Collection} specified in the {@link Multimap} interface.
     */
    SortedSet<V> get(K key);

    /**
     * Removes all values associated with a given key.
     * <p/>
     * <p>Because a {@code SortedSetMultimap} has unique sorted values for a given
     * key, this method returns a {@link SortedSet}, instead of the
     * {@link java.util.Collection} specified in the {@link Multimap} interface.
     */
    SortedSet<V> removeAll(Object key);

    /**
     * Stores a collection of values with the same key, replacing any existing
     * values for that key.
     * <p/>
     * <p>Because a {@code SortedSetMultimap} has unique sorted values for a given
     * key, this method returns a {@link SortedSet}, instead of the
     * {@link java.util.Collection} specified in the {@link Multimap} interface.
     * <p/>
     * <p>Any duplicates in {@code values} will be stored in the multimap once.
     */
    SortedSet<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * Returns a map view that associates each key with the corresponding values
     * in the multimap. Changes to the returned map, such as element removal,
     * will update the underlying multimap. The map never supports
     * {@code setValue()} on the map entries, {@code put}, or {@code putAll}.
     * <p/>
     * <p>The collections returned by {@code asMap().get(Object)} have the same
     * behavior as those returned by {@link #get}.
     * <p/>
     * <p>Though the method signature doesn't say so explicitly, the returned map
     * has {@link SortedSet} values.
     */
    Map<K, Collection<V>> asMap();

    /**
     * Returns the comparator that orders the multimap values, with a {@code null}
     * indicating that natural ordering is used.
     */
    Comparator<? super V> valueComparator();
}
