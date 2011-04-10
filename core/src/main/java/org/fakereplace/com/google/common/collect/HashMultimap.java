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
import org.fakereplace.com.google.common.annotations.VisibleForTesting;
import org.fakereplace.com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link Multimap} using hash tables.
 * <p/>
 * <p>The multimap does not store duplicate key-value pairs. Adding a new
 * key-value pair equal to an existing key-value pair has no effect.
 * <p/>
 * <p>Keys and values may be null. All optional multimap methods are supported,
 * and all returned views are modifiable.
 * <p/>
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedSetMultimap}.
 *
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
public final class HashMultimap<K, V> extends AbstractSetMultimap<K, V> {
    private static final int DEFAULT_VALUES_PER_KEY = 8;

    @VisibleForTesting
    transient int expectedValuesPerKey = DEFAULT_VALUES_PER_KEY;

    /**
     * Creates a new, empty {@code HashMultimap} with the default initial
     * capacities.
     */
    public static <K, V> HashMultimap<K, V> create() {
        return new HashMultimap<K, V>();
    }

    /**
     * Constructs an empty {@code HashMultimap} with enough capacity to hold the
     * specified numbers of keys and values without rehashing.
     *
     * @param expectedKeys         the expected number of distinct keys
     * @param expectedValuesPerKey the expected average number of values per key
     * @throws IllegalArgumentException if {@code expectedKeys} or {@code
     *                                  expectedValuesPerKey} is negative
     */
    public static <K, V> HashMultimap<K, V> create(
            int expectedKeys, int expectedValuesPerKey) {
        return new HashMultimap<K, V>(expectedKeys, expectedValuesPerKey);
    }

    /**
     * Constructs a {@code HashMultimap} with the same mappings as the specified
     * multimap. If a key-value mapping appears multiple times in the input
     * multimap, it only appears once in the constructed multimap.
     *
     * @param multimap the multimap whose contents are copied to this multimap
     */
    public static <K, V> HashMultimap<K, V> create(
            Multimap<? extends K, ? extends V> multimap) {
        return new HashMultimap<K, V>(multimap);
    }

    private HashMultimap() {
        super(new HashMap<K, Collection<V>>());
    }

    private HashMultimap(int expectedKeys, int expectedValuesPerKey) {
        super(Maps.<K, Collection<V>>newHashMapWithExpectedSize(expectedKeys));
        Preconditions.checkArgument(expectedValuesPerKey >= 0);
        this.expectedValuesPerKey = expectedValuesPerKey;
    }

    private HashMultimap(Multimap<? extends K, ? extends V> multimap) {
        super(Maps.<K, Collection<V>>newHashMapWithExpectedSize(
                multimap.keySet().size()));
        putAll(multimap);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Creates an empty {@code HashSet} for a collection of values for one key.
     *
     * @return a new {@code HashSet} containing a collection of values for one key
     */
    @Override
    Set<V> createCollection() {
        return Sets.<V>newHashSetWithExpectedSize(expectedValuesPerKey);
    }

    /**
     * @serialData expectedValuesPerKey, number of distinct keys, and then for
     * each distinct key: the key, number of values for that key, and the
     * key's values
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(expectedValuesPerKey);
        Serialization.writeMultimap(this, stream);
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        expectedValuesPerKey = stream.readInt();
        int distinctKeys = Serialization.readCount(stream);
        Map<K, Collection<V>> map = Maps.newHashMapWithExpectedSize(distinctKeys);
        setMap(map);
        Serialization.populateMultimap(this, stream, distinctKeys);
    }

    private static final long serialVersionUID = 0;
}
