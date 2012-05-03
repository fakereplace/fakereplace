/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fakereplace.com.google.common.collect;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * A multimap which forwards all its method calls to another multimap.
 * Subclasses should override one or more methods to modify the behavior of
 * the backing multimap as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Robert Konigsberg
 * @see ForwardingObject
 */
@GwtCompatible
public abstract class ForwardingMultimap<K, V> extends ForwardingObject
        implements Multimap<K, V> {

    @Override
    protected abstract Multimap<K, V> delegate();

    public Map<K, Collection<V>> asMap() {
        return delegate().asMap();
    }

    public void clear() {
        delegate().clear();
    }

    public boolean containsEntry(Object key, Object value) {
        return delegate().containsEntry(key, value);
    }

    public boolean containsKey(Object key) {
        return delegate().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate().containsValue(value);
    }

    public Collection<Entry<K, V>> entries() {
        return delegate().entries();
    }

    public Collection<V> get(K key) {
        return delegate().get(key);
    }

    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    public Multiset<K> keys() {
        return delegate().keys();
    }

    public Set<K> keySet() {
        return delegate().keySet();
    }

    public boolean put(K key, V value) {
        return delegate().put(key, value);
    }

    public boolean putAll(K key, Iterable<? extends V> values) {
        return delegate().putAll(key, values);
    }

    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
        return delegate().putAll(multimap);
    }

    public boolean remove(Object key, Object value) {
        return delegate().remove(key, value);
    }

    public Collection<V> removeAll(Object key) {
        return delegate().removeAll(key);
    }

    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
        return delegate().replaceValues(key, values);
    }

    public int size() {
        return delegate().size();
    }

    public Collection<V> values() {
        return delegate().values();
    }

    @Override
    public boolean equals(Object object) {
        return object == this || delegate().equals(object);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }
}
