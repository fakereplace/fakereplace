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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * Multiset implementation backed by a {@link HashMap}.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
public final class HashMultiset<E> extends AbstractMapBasedMultiset<E> {

    /**
     * Creates a new, empty {@code HashMultiset} using the default initial
     * capacity.
     */
    public static <E> HashMultiset<E> create() {
        return new HashMultiset<E>();
    }

    /**
     * Creates a new, empty {@code HashMultiset} with the specified expected
     * number of distinct elements.
     *
     * @param distinctElements the expected number of distinct elements
     * @throws IllegalArgumentException if {@code distinctElements} is negative
     */
    public static <E> HashMultiset<E> create(int distinctElements) {
        return new HashMultiset<E>(distinctElements);
    }

    /**
     * Creates a new {@code HashMultiset} containing the specified elements.
     *
     * @param elements the elements that the multiset should contain
     */
    public static <E> HashMultiset<E> create(Iterable<? extends E> elements) {
        HashMultiset<E> multiset =
                create(Multisets.inferDistinctElements(elements));
        Iterables.addAll(multiset, elements);
        return multiset;
    }

    private HashMultiset() {
        super(new HashMap<E, AtomicInteger>());
    }

    private HashMultiset(int distinctElements) {
        super(new HashMap<E, AtomicInteger>(Maps.capacity(distinctElements)));
    }

    /**
     * @serialData the number of distinct elements, the first element, its count,
     * the second element, its count, and so on
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        Serialization.writeMultiset(this, stream);
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        int distinctElements = Serialization.readCount(stream);
        setBackingMap(
                Maps.<E, AtomicInteger>newHashMapWithExpectedSize(distinctElements));
        Serialization.populateMultiset(this, stream, distinctElements);
    }

    private static final long serialVersionUID = 0;
}
