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
import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.base.Objects;
import org.fakereplace.com.google.common.base.Preconditions;

import java.io.Serializable;


/**
 * An ordering that orders elements by applying an order to the result of a
 * function on those elements.
 */
@GwtCompatible(serializable = true)
final class ByFunctionOrdering<F, T>
        extends Ordering<F> implements Serializable {
    final Function<F, ? extends T> function;
    final Ordering<T> ordering;

    ByFunctionOrdering(
            Function<F, ? extends T> function, Ordering<T> ordering) {
        this.function = Preconditions.checkNotNull(function);
        this.ordering = Preconditions.checkNotNull(ordering);
    }

    public int compare(F left, F right) {
        return ordering.compare(function.apply(left), function.apply(right));
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ByFunctionOrdering) {
            ByFunctionOrdering<?, ?> that = (ByFunctionOrdering<?, ?>) object;
            return this.function.equals(that.function)
                    && this.ordering.equals(that.ordering);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(function, ordering);
    }

    @Override
    public String toString() {
        return ordering + ".onResultOf(" + function + ")";
    }

    private static final long serialVersionUID = 0;
}
