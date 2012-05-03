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
import java.util.List;
import java.util.ListIterator;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.annotations.GwtIncompatible;


/**
 * A list which forwards all its method calls to another list. Subclasses should
 * override one or more methods to modify the behavior of the backing list as
 * desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * <p/>
 * <p>This class does not implement {@link java.util.RandomAccess}. If the
 * delegate supports random access, the {@code ForwadingList} subclass should
 * implement the {@code RandomAccess} interface.
 *
 * @author Mike Bostock
 */
@GwtCompatible
public abstract class ForwardingList<E> extends ForwardingCollection<E>
        implements List<E> {

    @Override
    protected abstract List<E> delegate();

    public void add(int index, E element) {
        delegate().add(index, element);
    }

    public boolean addAll(int index, Collection<? extends E> elements) {
        return delegate().addAll(index, elements);
    }

    public E get(int index) {
        return delegate().get(index);
    }

    public int indexOf(Object element) {
        return delegate().indexOf(element);
    }

    public int lastIndexOf(Object element) {
        return delegate().lastIndexOf(element);
    }

    public ListIterator<E> listIterator() {
        return delegate().listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return delegate().listIterator(index);
    }

    public E remove(int index) {
        return delegate().remove(index);
    }

    public E set(int index, E element) {
        return delegate().set(index, element);
    }

    @GwtIncompatible("List.subList")
    public List<E> subList(int fromIndex, int toIndex) {
        return Platform.subList(delegate(), fromIndex, toIndex);
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
