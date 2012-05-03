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

import org.fakereplace.com.google.common.annotations.GwtCompatible;

import static org.fakereplace.com.google.common.base.Preconditions.checkArgument;

/**
 * Static methods for implementing hash-based collections.
 *
 * @author Kevin Bourrillion
 * @author Jesse Wilson
 */
@GwtCompatible
final class Hashing {
    private Hashing() {
    }

    /*
    * This method was written by Doug Lea with assistance from members of JCP
    * JSR-166 Expert Group and released to the public domain, as explained at
    * http://creativecommons.org/licenses/publicdomain
    */
    static int smear(int hashCode) {
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }

    // We use power-of-2 tables, and this is the highest int that's a power of 2
    private static final int MAX_TABLE_SIZE = 1 << 30;

    // If the set has this many elements, it will "max out" the table size
    private static final int CUTOFF = 1 << 29;

    // Size the table to be at most 50% full, if possible
    static int chooseTableSize(int setSize) {
        if (setSize < CUTOFF) {
            return Integer.highestOneBit(setSize) << 2;
        }

        // The table can't be completely full or we'll get infinite reprobes
        checkArgument(setSize < MAX_TABLE_SIZE, "collection too large");
        return MAX_TABLE_SIZE;
    }
}
