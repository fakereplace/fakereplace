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

import org.fakereplace.com.google.common.annotations.GwtCompatible;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static org.fakereplace.com.google.common.base.Preconditions.*;


/**
 * An empty immutable list.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(serializable = true)
final class EmptyImmutableList extends ImmutableList<Object> {
    static final EmptyImmutableList INSTANCE = new EmptyImmutableList();

    private EmptyImmutableList() {
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
    public UnmodifiableIterator<Object> iterator() {
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

    public Object get(int index) {
        // guaranteed to fail, but at least we get a consistent message
        checkElementIndex(index, 0);
        throw new AssertionError("unreachable");
    }

    @Override
    public int indexOf(Object target) {
        return -1;
    }

    @Override
    public int lastIndexOf(Object target) {
        return -1;
    }

    @Override
    public ImmutableList<Object> subList(int fromIndex, int toIndex) {
        checkPositionIndexes(fromIndex, toIndex, 0);
        return this;
    }

    public ListIterator<Object> listIterator() {
        return Collections.emptyList().listIterator();
    }

    public ListIterator<Object> listIterator(int start) {
        checkPositionIndex(start, 0);
        return Collections.emptyList().listIterator();
    }

    @Override
    public boolean containsAll(Collection<?> targets) {
        return targets.isEmpty();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof List) {
            List<?> that = (List<?>) object;
            return that.isEmpty();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "[]";
    }

    Object readResolve() {
        return INSTANCE; // preserve singleton property
    }

    private static final long serialVersionUID = 0;
}
