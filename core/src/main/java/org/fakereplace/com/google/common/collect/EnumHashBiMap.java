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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


/**
 * A {@code BiMap} backed by an {@code EnumMap} instance for keys-to-values, and
 * a {@code HashMap} instance for values-to-keys. Null keys are not permitted,
 * but null values are. An {@code EnumHashBiMap} and its inverse are both
 * serializable.
 *
 * @author Mike Bostock
 */
public final class EnumHashBiMap<K extends Enum<K>, V>
        extends AbstractBiMap<K, V> {
    private transient Class<K> keyType;

    /**
     * Returns a new, empty {@code EnumHashBiMap} using the specified key type.
     *
     * @param keyType the key type
     */
    public static <K extends Enum<K>, V> EnumHashBiMap<K, V>
    create(Class<K> keyType) {
        return new EnumHashBiMap<K, V>(keyType);
    }

    /**
     * Constructs a new bimap with the same mappings as the specified map. If the
     * specified map is an {@code EnumHashBiMap} or an {@link EnumBiMap}, the new
     * bimap has the same key type as the input bimap. Otherwise, the specified
     * map must contain at least one mapping, in order to determine the key type.
     *
     * @param map the map whose mappings are to be placed in this map
     * @throws IllegalArgumentException if map is not an {@code EnumBiMap} or an
     *                                  {@code EnumHashBiMap} instance and contains no mappings
     */
    public static <K extends Enum<K>, V> EnumHashBiMap<K, V>
    create(Map<K, ? extends V> map) {
        EnumHashBiMap<K, V> bimap = create(EnumBiMap.inferKeyType(map));
        bimap.putAll(map);
        return bimap;
    }

    private EnumHashBiMap(Class<K> keyType) {
        super(new EnumMap<K, V>(keyType), Maps.<V, K>newHashMapWithExpectedSize(
                keyType.getEnumConstants().length));
        this.keyType = keyType;
    }

    // Overriding these two methods to show that values may be null (but not keys)

    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public V forcePut(K key, V value) {
        return super.forcePut(key, value);
    }

    /**
     * Returns the associated key type.
     */
    public Class<K> keyType() {
        return keyType;
    }

    /**
     * @serialData the key class, number of entries, first key, first value,
     * second key, second value, and so on.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(keyType);
        Serialization.writeMap(this, stream);
    }

    @SuppressWarnings("unchecked") // reading field populated by writeObject
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        keyType = (Class<K>) stream.readObject();
        setDelegates(new EnumMap<K, V>(keyType),
                new HashMap<V, K>(keyType.getEnumConstants().length * 3 / 2));
        Serialization.populateMap(this, stream);
    }

    private static final long serialVersionUID = 0;
}
