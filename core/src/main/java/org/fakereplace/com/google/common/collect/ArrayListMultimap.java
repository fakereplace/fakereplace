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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.annotations.VisibleForTesting;
import org.fakereplace.com.google.common.base.Preconditions;

/**
 * Implementation of {@code Multimap} that uses an {@code ArrayList} to store
 * the values for a given key. A {@link HashMap} associates each key with an
 * {@link ArrayList} of values.
 * <p/>
 * <p>When iterating through the collections supplied by this class, the
 * ordering of values for a given key agrees with the order in which the values
 * were added.
 * <p/>
 * <p>This multimap allows duplicate key-value pairs. After adding a new
 * key-value pair equal to an existing key-value pair, the {@code
 * ArrayListMultimap} will contain entries for both the new value and the old
 * value.
 * <p/>
 * <p>Keys and values may be null. All optional multimap methods are supported,
 * and all returned views are modifiable.
 * <p/>
 * <p>The lists returned by {@link #get}, {@link #removeAll}, and {@link
 * #replaceValues} all implement {@link java.util.RandomAccess}.
 * <p/>
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedListMultimap}.
 *
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
public final class ArrayListMultimap<K, V> extends AbstractListMultimap<K, V> {
    // Default from ArrayList
    private static final int DEFAULT_VALUES_PER_KEY = 10;

    @VisibleForTesting
    transient int expectedValuesPerKey;

    /**
     * Creates a new, empty {@code ArrayListMultimap} with the default initial
     * capacities.
     */
    public static <K, V> ArrayListMultimap<K, V> create() {
        return new ArrayListMultimap<K, V>();
    }

    /**
     * Constructs an empty {@code ArrayListMultimap} with enough capacity to hold
     * the specified numbers of keys and values without resizing.
     *
     * @param expectedKeys         the expected number of distinct keys
     * @param expectedValuesPerKey the expected average number of values per key
     * @throws IllegalArgumentException if {@code expectedKeys} or {@code
     *                                  expectedValuesPerKey} is negative
     */
    public static <K, V> ArrayListMultimap<K, V> create(
            int expectedKeys, int expectedValuesPerKey) {
        return new ArrayListMultimap<K, V>(expectedKeys, expectedValuesPerKey);
    }

    /**
     * Constructs an {@code ArrayListMultimap} with the same mappings as the
     * specified multimap.
     *
     * @param multimap the multimap whose contents are copied to this multimap
     */
    public static <K, V> ArrayListMultimap<K, V> create(
            Multimap<? extends K, ? extends V> multimap) {
        return new ArrayListMultimap<K, V>(multimap);
    }

    private ArrayListMultimap() {
        super(new HashMap<K, Collection<V>>());
        expectedValuesPerKey = DEFAULT_VALUES_PER_KEY;
    }

    private ArrayListMultimap(int expectedKeys, int expectedValuesPerKey) {
        super(Maps.<K, Collection<V>>newHashMapWithExpectedSize(expectedKeys));
        Preconditions.checkArgument(expectedValuesPerKey >= 0);
        this.expectedValuesPerKey = expectedValuesPerKey;
    }

    private ArrayListMultimap(Multimap<? extends K, ? extends V> multimap) {
        this(multimap.keySet().size(),
                (multimap instanceof ArrayListMultimap) ?
                        ((ArrayListMultimap<?, ?>) multimap).expectedValuesPerKey :
                        DEFAULT_VALUES_PER_KEY);
        putAll(multimap);
    }

    /**
     * Creates a new, empty {@code ArrayList} to hold the collection of values for
     * an arbitrary key.
     */
    @Override
    List<V> createCollection() {
        return new ArrayList<V>(expectedValuesPerKey);
    }

    /**
     * Reduces the memory used by this {@code ArrayListMultimap}, if feasible.
     */
    public void trimToSize() {
        for (Collection<V> collection : backingMap().values()) {
            ArrayList<V> arrayList = (ArrayList<V>) collection;
            arrayList.trimToSize();
        }
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
