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

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * An empty immutable sorted set.
 *
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial")
        // uses writeReplace(), not default serialization
class EmptyImmutableSortedSet<E> extends ImmutableSortedSet<E> {
    EmptyImmutableSortedSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object target) {
        return false;
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return Iterators.emptyIterator();
    }

    private static final Object[] EMPTY_ARRAY = new Object[0];

    @Override
    public Object[] toArray() {
        return EMPTY_ARRAY;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length > 0) {
            a[0] = null;
        }
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> targets) {
        return targets.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Set) {
            Set<?> that = (Set<?>) object;
            return that.isEmpty();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "[]";
    }

    public E first() {
        throw new NoSuchElementException();
    }

    public E last() {
        throw new NoSuchElementException();
    }

    @Override
    ImmutableSortedSet<E> headSetImpl(E toElement) {
        return this;
    }

    @Override
    ImmutableSortedSet<E> subSetImpl(E fromElement, E toElement) {
        return this;
    }

    @Override
    ImmutableSortedSet<E> tailSetImpl(E fromElement) {
        return this;
    }

    @Override
    boolean hasPartialArray() {
        return false;
    }
}
