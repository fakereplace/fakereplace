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

import java.io.Serializable;

import static org.fakereplace.com.google.common.base.Preconditions.checkNotNull;

/**
 * An ordering that uses the reverse of the natural order of the values.
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("unchecked") // TODO: the right way to explain this??
final class ReverseNaturalOrdering
        extends Ordering<Comparable> implements Serializable {
    static final ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering();

    public int compare(Comparable left, Comparable right) {
        checkNotNull(left); // right null is caught later
        if (left == right) {
            return 0;
        }

        @SuppressWarnings("unchecked") // we're permitted to throw CCE
                int result = right.compareTo(left);
        return result;
    }

    @Override
    public <S extends Comparable> Ordering<S> reverse() {
        return natural();
    }

    // Override the six min/max methods to "hoist" delegation outside loops

    @Override
    public <E extends Comparable> E min(E a, E b) {
        return NaturalOrdering.INSTANCE.max(a, b);
    }

    @Override
    public <E extends Comparable> E min(E a, E b, E c, E... rest) {
        return NaturalOrdering.INSTANCE.max(a, b, c, rest);
    }

    @Override
    public <E extends Comparable> E min(Iterable<E> iterable) {
        return NaturalOrdering.INSTANCE.max(iterable);
    }

    @Override
    public <E extends Comparable> E max(E a, E b) {
        return NaturalOrdering.INSTANCE.min(a, b);
    }

    @Override
    public <E extends Comparable> E max(E a, E b, E c, E... rest) {
        return NaturalOrdering.INSTANCE.min(a, b, c, rest);
    }

    @Override
    public <E extends Comparable> E max(Iterable<E> iterable) {
        return NaturalOrdering.INSTANCE.min(iterable);
    }

    // preserving singleton-ness gives equals()/hashCode() for free
    private Object readResolve() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Ordering.natural().reverse()";
    }

    private ReverseNaturalOrdering() {
    }

    private static final long serialVersionUID = 0;
}
