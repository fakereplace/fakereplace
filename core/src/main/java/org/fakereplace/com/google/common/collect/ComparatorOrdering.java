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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

import static org.fakereplace.com.google.common.base.Preconditions.checkNotNull;


/**
 * An ordering for a pre-existing {@code comparator}.
 */
@GwtCompatible(serializable = true)
final class ComparatorOrdering<T> extends Ordering<T> implements Serializable {
    final Comparator<T> comparator;

    ComparatorOrdering(Comparator<T> comparator) {
        this.comparator = checkNotNull(comparator);
    }

    public int compare(T a, T b) {
        return comparator.compare(a, b);
    }

    // Override just to remove a level of indirection from inner loops
    @Override
    public int binarySearch(List<? extends T> sortedList, T key) {
        return Collections.binarySearch(sortedList, key, comparator);
    }

    // Override just to remove a level of indirection from inner loops
    @Override
    public <E extends T> List<E> sortedCopy(Iterable<E> iterable) {
        List<E> list = Lists.newArrayList(iterable);
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ComparatorOrdering) {
            ComparatorOrdering<?> that = (ComparatorOrdering<?>) object;
            return this.comparator.equals(that.comparator);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return comparator.hashCode();
    }

    @Override
    public String toString() {
        return comparator.toString();
    }

    private static final long serialVersionUID = 0;
}
