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

import static org.fakereplace.com.google.common.base.Preconditions.checkNotNull;


/**
 * An ordering that uses the reverse of a given order.
 */
@GwtCompatible(serializable = true)
final class ReverseOrdering<T> extends Ordering<T> implements Serializable {
    final Ordering<? super T> forwardOrder;

    ReverseOrdering(Ordering<? super T> forwardOrder) {
        this.forwardOrder = checkNotNull(forwardOrder);
    }

    public int compare(T a, T b) {
        return forwardOrder.compare(b, a);
    }

    @SuppressWarnings("unchecked") // how to explain?
    @Override
    public <S extends T> Ordering<S> reverse() {
        return (Ordering) forwardOrder;
    }

    // Override the six min/max methods to "hoist" delegation outside loops

    @Override
    public <E extends T> E min(E a, E b) {
        return forwardOrder.max(a, b);
    }

    @Override
    public <E extends T> E min(E a, E b, E c, E... rest) {
        return forwardOrder.max(a, b, c, rest);
    }

    @Override
    public <E extends T> E min(Iterable<E> iterable) {
        return forwardOrder.max(iterable);
    }

    @Override
    public <E extends T> E max(E a, E b) {
        return forwardOrder.min(a, b);
    }

    @Override
    public <E extends T> E max(E a, E b, E c, E... rest) {
        return forwardOrder.min(a, b, c, rest);
    }

    @Override
    public <E extends T> E max(Iterable<E> iterable) {
        return forwardOrder.min(iterable);
    }

    @Override
    public int hashCode() {
        return -forwardOrder.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ReverseOrdering) {
            ReverseOrdering<?> that = (ReverseOrdering<?>) object;
            return this.forwardOrder.equals(that.forwardOrder);
        }
        return false;
    }

    @Override
    public String toString() {
        return forwardOrder + ".reverse()";
    }

    private static final long serialVersionUID = 0;
}
