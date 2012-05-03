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

import java.util.Queue;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * A queue which forwards all its method calls to another queue. Subclasses
 * should override one or more methods to modify the behavior of the backing
 * queue as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Mike Bostock
 * @see ForwardingObject
 */
@GwtCompatible
public abstract class ForwardingQueue<E> extends ForwardingCollection<E>
        implements Queue<E> {

    @Override
    protected abstract Queue<E> delegate();

    public boolean offer(E o) {
        return delegate().offer(o);
    }

    public E poll() {
        return delegate().poll();
    }

    public E remove() {
        return delegate().remove();
    }

    public E peek() {
        return delegate().peek();
    }

    public E element() {
        return delegate().element();
    }
}
