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

import java.util.Iterator;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * An iterator that does not support {@link #remove}.
 *
 * @author Jared Levy
 */
@GwtCompatible
public abstract class UnmodifiableIterator<E> implements Iterator<E> {
    /**
     * Guaranteed to throw an exception and leave the underlying data unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
