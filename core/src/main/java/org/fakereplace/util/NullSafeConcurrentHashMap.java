/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.fakereplace.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map implementation that wraps ConcurrentHashMap
 * but allows for null values in put and get
 *
 * @param <K>
 * @param <V>
 * @author stuart
 */
public class NullSafeConcurrentHashMap<K, V> implements Map<K, V>, Serializable {
    final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();

    final Serializable NULL_VALUE = new Serializable() {
    };

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.contains(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public V get(Object key) {
        V val = map.get(key);
        if (val == NULL_VALUE) {
            return null;
        }
        return val;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public V put(K key, V value) {
        if (value == null) {
            value = (V) NULL_VALUE;
        }
        return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    public V remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<V> values() {
        return map.values();
    }
}
