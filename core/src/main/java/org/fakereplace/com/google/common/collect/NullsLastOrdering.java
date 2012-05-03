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

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * An ordering that treats {@code null} as greater than all other values.
 */
@GwtCompatible(serializable = true)
final class NullsLastOrdering<T> extends Ordering<T> implements Serializable {
    final Ordering<? super T> ordering;

    NullsLastOrdering(Ordering<? super T> ordering) {
        this.ordering = ordering;
    }

    public int compare(T left, T right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return LEFT_IS_GREATER;
        }
        if (right == null) {
            return RIGHT_IS_GREATER;
        }
        return ordering.compare(left, right);
    }

    @Override
    public <S extends T> Ordering<S> reverse() {
        // ordering.reverse() might be optimized, so let it do its thing
        return ordering.reverse().nullsFirst();
    }

    @Override
    public <S extends T> Ordering<S> nullsFirst() {
        return ordering.nullsFirst();
    }

    @SuppressWarnings("unchecked") // still need the right way to explain this
    @Override
    public <S extends T> Ordering<S> nullsLast() {
        return (Ordering) this;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof NullsLastOrdering) {
            NullsLastOrdering<?> that = (NullsLastOrdering<?>) object;
            return this.ordering.equals(that.ordering);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ordering.hashCode() ^ -921210296; // meaningless
    }

    @Override
    public String toString() {
        return ordering + ".nullsLast()";
    }

    private static final long serialVersionUID = 0;
}
