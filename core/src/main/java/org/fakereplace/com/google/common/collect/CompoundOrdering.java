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
import java.util.Comparator;
import java.util.List;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * An ordering that tries several comparators in order.
 */
@GwtCompatible(serializable = true)
final class CompoundOrdering<T> extends Ordering<T> implements Serializable {
    final ImmutableList<Comparator<? super T>> comparators;

    CompoundOrdering(Comparator<? super T> primary,
                     Comparator<? super T> secondary) {
        this.comparators
                = ImmutableList.<Comparator<? super T>>of(primary, secondary);
    }

    CompoundOrdering(Iterable<? extends Comparator<? super T>> comparators) {
        this.comparators = ImmutableList.copyOf(comparators);
    }

    CompoundOrdering(List<? extends Comparator<? super T>> comparators,
                     Comparator<? super T> lastComparator) {
        this.comparators = new ImmutableList.Builder<Comparator<? super T>>()
                .addAll(comparators).add(lastComparator).build();
    }

    public int compare(T left, T right) {
        for (Comparator<? super T> comparator : comparators) {
            int result = comparator.compare(left, right);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CompoundOrdering) {
            CompoundOrdering<?> that = (CompoundOrdering<?>) object;
            return this.comparators.equals(that.comparators);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return comparators.hashCode();
    }

    @Override
    public String toString() {
        return "Ordering.compound(" + comparators + ")";
    }

    private static final long serialVersionUID = 0;
}
