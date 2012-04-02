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

import java.util.Set;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * A multiset which forwards all its method calls to another multiset.
 * Subclasses should override one or more methods to modify the behavior of the
 * backing multiset as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Kevin Bourrillion
 * @see ForwardingObject
 */
@GwtCompatible
public abstract class ForwardingMultiset<E> extends ForwardingCollection<E>
        implements Multiset<E> {

    @Override
    protected abstract Multiset<E> delegate();

    public int count(Object element) {
        return delegate().count(element);
    }

    public int add(E element, int occurrences) {
        return delegate().add(element, occurrences);
    }

    public int remove(Object element, int occurrences) {
        return delegate().remove(element, occurrences);
    }

    public Set<E> elementSet() {
        return delegate().elementSet();
    }

    public Set<Entry<E>> entrySet() {
        return delegate().entrySet();
    }

    @Override
    public boolean equals(Object object) {
        return object == this || delegate().equals(object);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    public int setCount(E element, int count) {
        return delegate().setCount(element, count);
    }

    public boolean setCount(E element, int oldCount, int newCount) {
        return delegate().setCount(element, oldCount, newCount);
    }
}
