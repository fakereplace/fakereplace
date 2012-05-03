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
import java.util.List;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * An ordering that compares objects according to a given order.
 */
@GwtCompatible(serializable = true)
final class ExplicitOrdering<T> extends Ordering<T> implements Serializable {
    final ImmutableMap<T, Integer> rankMap;

    ExplicitOrdering(List<T> valuesInOrder) {
        this(buildRankMap(valuesInOrder));
    }

    ExplicitOrdering(ImmutableMap<T, Integer> rankMap) {
        this.rankMap = rankMap;
    }

    public int compare(T left, T right) {
        return rank(left) - rank(right); // safe because both are nonnegative
    }

    private int rank(T value) {
        Integer rank = rankMap.get(value);
        if (rank == null) {
            throw new IncomparableValueException(value);
        }
        return rank;
    }

    private static <T> ImmutableMap<T, Integer> buildRankMap(
            List<T> valuesInOrder) {
        ImmutableMap.Builder<T, Integer> builder = ImmutableMap.builder();
        int rank = 0;
        for (T value : valuesInOrder) {
            builder.put(value, rank++);
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ExplicitOrdering) {
            ExplicitOrdering<?> that = (ExplicitOrdering<?>) object;
            return this.rankMap.equals(that.rankMap);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return rankMap.hashCode();
    }

    @Override
    public String toString() {
        return "Ordering.explicit(" + rankMap.keySet() + ")";
    }

    private static final long serialVersionUID = 0;
}
