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

import java.util.Set;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.base.Preconditions;


/**
 * Implementation of {@link ImmutableSet} with exactly one element.
 *
 * @author Kevin Bourrillion
 * @author Nick Kralevich
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
final class SingletonImmutableSet<E> extends ImmutableSet<E> {
    final transient E element;

    // Non-volatile because:
    //   - Integer is immutable and thus thread-safe;
    //   - no problems if one thread overwrites the cachedHashCode from another.
    private transient Integer cachedHashCode;

    SingletonImmutableSet(E element) {
        this.element = Preconditions.checkNotNull(element);
    }

    SingletonImmutableSet(E element, int hashCode) {
        // Guaranteed to be non-null by the presence of the pre-computed hash code.
        this.element = element;
        cachedHashCode = hashCode;
    }

    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object target) {
        return element.equals(target);
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(element);
    }

    @Override
    public Object[] toArray() {
        return new Object[]{element};
    }

    @SuppressWarnings({"unchecked"})
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

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Set) {
            Set<?> that = (Set<?>) object;
            return that.size() == 1 && element.equals(that.iterator().next());
        }
        return false;
    }

    @Override
    public int hashCode() {
        Integer code = cachedHashCode;
        if (code == null) {
            return cachedHashCode = element.hashCode();
        }
        return code;
    }

    @Override
    boolean isHashCodeFast() {
        return false;
    }

    @Override
    public String toString() {
        String elementToString = element.toString();
        return new StringBuilder(elementToString.length() + 2)
                .append('[')
                .append(elementToString)
                .append(']')
                .toString();
    }
}
