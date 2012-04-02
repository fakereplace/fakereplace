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

import java.util.Comparator;
import java.util.SortedSet;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * A sorted set which forwards all its method calls to another sorted set.
 * Subclasses should override one or more methods to modify the behavior of the
 * backing sorted set as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Mike Bostock
 * @see ForwardingObject
 */
@GwtCompatible
public abstract class ForwardingSortedSet<E> extends ForwardingSet<E>
        implements SortedSet<E> {

    @Override
    protected abstract SortedSet<E> delegate();

    public Comparator<? super E> comparator() {
        return delegate().comparator();
    }

    public E first() {
        return delegate().first();
    }

    public SortedSet<E> headSet(E toElement) {
        return delegate().headSet(toElement);
    }

    public E last() {
        return delegate().last();
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return delegate().subSet(fromElement, toElement);
    }

    public SortedSet<E> tailSet(E fromElement) {
        return delegate().tailSet(fromElement);
    }
}
