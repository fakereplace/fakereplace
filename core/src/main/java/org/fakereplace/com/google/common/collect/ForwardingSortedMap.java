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

import java.util.Comparator;
import java.util.SortedMap;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * A sorted map which forwards all its method calls to another sorted map.
 * Subclasses should override one or more methods to modify the behavior of
 * the backing sorted map as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Mike Bostock
 * @see ForwardingObject
 */
@GwtCompatible
public abstract class ForwardingSortedMap<K, V> extends ForwardingMap<K, V>
        implements SortedMap<K, V> {

    @Override
    protected abstract SortedMap<K, V> delegate();

    public Comparator<? super K> comparator() {
        return delegate().comparator();
    }

    public K firstKey() {
        return delegate().firstKey();
    }

    public SortedMap<K, V> headMap(K toKey) {
        return delegate().headMap(toKey);
    }

    public K lastKey() {
        return delegate().lastKey();
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return delegate().subMap(fromKey, toKey);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
        return delegate().tailMap(fromKey);
    }
}
