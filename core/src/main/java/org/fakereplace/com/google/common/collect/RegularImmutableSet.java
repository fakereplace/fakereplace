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
import org.fakereplace.com.google.common.annotations.VisibleForTesting;

/**
 * Implementation of {@link ImmutableSet} with two or more elements.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
final class RegularImmutableSet<E> extends ImmutableSet.ArrayImmutableSet<E> {
    // the same elements in hashed positions (plus nulls)
    @VisibleForTesting
    final transient Object[] table;
    // 'and' with an int to get a valid table index.
    private final transient int mask;
    private final transient int hashCode;

    RegularImmutableSet(
            Object[] elements, int hashCode, Object[] table, int mask) {
        super(elements);
        this.table = table;
        this.mask = mask;
        this.hashCode = hashCode;
    }

    @Override
    public boolean contains(Object target) {
        if (target == null) {
            return false;
        }
        for (int i = Hashing.smear(target.hashCode()); true; i++) {
            Object candidate = table[i & mask];
            if (candidate == null) {
                return false;
            }
            if (candidate.equals(target)) {
                return true;
            }
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    boolean isHashCodeFast() {
        return true;
    }
}
