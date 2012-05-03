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
import java.util.List;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

import static org.fakereplace.com.google.common.base.Preconditions.checkNotNull;

/**
 * An ordering that uses the natural order of the values.
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("unchecked") // TODO: the right way to explain this??
final class NaturalOrdering
        extends Ordering<Comparable> implements Serializable {
    static final NaturalOrdering INSTANCE = new NaturalOrdering();

    public int compare(Comparable left, Comparable right) {
        checkNotNull(right); // left null is caught later
        if (left == right) {
            return 0;
        }

        @SuppressWarnings("unchecked") // we're permitted to throw CCE
                int result = left.compareTo(right);
        return result;
    }

    @SuppressWarnings("unchecked") // TODO: the right way to explain this??
    @Override
    public <S extends Comparable> Ordering<S> reverse() {
        return (Ordering) ReverseNaturalOrdering.INSTANCE;
    }

    // Override to remove a level of indirection from inner loop
    @SuppressWarnings("unchecked") // TODO: the right way to explain this??
    @Override
    public int binarySearch(
            List<? extends Comparable> sortedList, Comparable key) {
        return Collections.binarySearch((List) sortedList, key);
    }

    // Override to remove a level of indirection from inner loop
    @Override
    public <E extends Comparable> List<E> sortedCopy(
            Iterable<E> iterable) {
        List<E> list = Lists.newArrayList(iterable);
        Collections.sort(list);
        return list;
    }

    // preserving singleton-ness gives equals()/hashCode() for free
    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Ordering.natural()";
    }

    private NaturalOrdering() {
    }

    private static final long serialVersionUID = 0;
}
