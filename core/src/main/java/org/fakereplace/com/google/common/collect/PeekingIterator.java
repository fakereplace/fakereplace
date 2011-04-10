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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that supports a one-element lookahead while iterating.
 *
 * @author Mick Killianey
 */
@GwtCompatible
public interface PeekingIterator<E> extends Iterator<E> {
    /**
     * Returns the next element in the iteration, without advancing the iteration.
     * <p/>
     * <p>Calls to {@code peek()} should not change the state of the iteration,
     * except that it <i>may</i> prevent removal of the most recent element via
     * {@link #remove()}.
     *
     * @throws NoSuchElementException if the iteration has no more elements
     *                                according to {@link #hasNext()}
     */
    E peek();

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The objects returned by consecutive calls to {@link #peek()} then {@link
     * #next()} are guaranteed to be equal to each other.
     */
    E next();

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Implementations may or may not support removal when a call to {@link
     * #peek()} has occurred since the most recent call to {@link #next()}.
     *
     * @throws IllegalStateException if there has been a call to {@link #peek()}
     *                               since the most recent call to {@link #next()} and this implementation
     *                               does not support this sequence of calls (optional)
     */
    void remove();
}
