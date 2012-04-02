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

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.base.Preconditions;

import static org.fakereplace.com.google.common.base.Preconditions.checkNotNull;


/**
 * Implementation of {@link ImmutableList} with exactly one element.
 *
 * @author Hayward Chan
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
final class SingletonImmutableList<E> extends ImmutableList<E> {
    final transient E element;

    SingletonImmutableList(E element) {
        this.element = checkNotNull(element);
    }

    public E get(int index) {
        Preconditions.checkElementIndex(index, 1);
        return element;
    }

    @Override
    public int indexOf(Object object) {
        return element.equals(object) ? 0 : -1;
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(element);
    }

    @Override
    public int lastIndexOf(Object object) {
        return element.equals(object) ? 0 : -1;
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int start) {
        // suboptimal but not worth optimizing.
        return Collections.singletonList(element).listIterator(start);
    }

    public int size() {
        return 1;
    }

    @Override
    public ImmutableList<E> subList(int fromIndex, int toIndex) {
        Preconditions.checkPositionIndexes(fromIndex, toIndex, 1);
        return (fromIndex == toIndex) ? (ImmutableList<E>) of() : this;
    }

    @Override
    public boolean contains(Object object) {
        return element.equals(object);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof List) {
            List<?> that = (List<?>) object;
            return that.size() == 1 && element.equals(that.get(0));
        }
        return false;
    }

    @Override
    public int hashCode() {
        // not caching hash code since it could change if the element is mutable
        // in a way that modifies its hash code.
        return 31 + element.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Object[] toArray() {
        return new Object[]{element};
    }

    @Override
    public <T> T[] toArray(T[] array) {
        if (array.length == 0) {
            array = ObjectArrays.newArray(array, 1);
        } else if (array.length > 1) {
            array[1] = null;
        }
        // Writes will produce ArrayStoreException when the toArray() doc requires.
        Object[] objectArray = array;
        objectArray[0] = element;
        return array;
    }
}
