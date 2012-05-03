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
import java.util.Iterator;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * A collection which forwards all its method calls to another collection.
 * Subclasses should override one or more methods to modify the behavior of
 * the backing collection as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Kevin Bourrillion
 * @see ForwardingObject
 */
@GwtCompatible
public abstract class ForwardingCollection<E> extends ForwardingObject
        implements Collection<E> {

    @Override
    protected abstract Collection<E> delegate();

    public Iterator<E> iterator() {
        return delegate().iterator();
    }

    public int size() {
        return delegate().size();
    }

    public boolean removeAll(Collection<?> collection) {
        return delegate().removeAll(collection);
    }

    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    public boolean contains(Object object) {
        return delegate().contains(object);
    }

    public Object[] toArray() {
        return delegate().toArray();
    }

    public <T> T[] toArray(T[] array) {
        return delegate().toArray(array);
    }

    public boolean add(E element) {
        return delegate().add(element);
    }

    public boolean remove(Object object) {
        return delegate().remove(object);
    }

    public boolean containsAll(Collection<?> collection) {
        return delegate().containsAll(collection);
    }

    public boolean addAll(Collection<? extends E> collection) {
        return delegate().addAll(collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return delegate().retainAll(collection);
    }

    public void clear() {
        delegate().clear();
    }
}
