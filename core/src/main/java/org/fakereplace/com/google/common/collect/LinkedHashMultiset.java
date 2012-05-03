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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.fakereplace.com.google.common.annotations.GwtCompatible;

/**
 * A {@code Multiset} implementation with predictable iteration order. Its
 * iterator orders elements according to when the first occurrence of the
 * element was added. When the multiset contains multiple instances of an
 * element, those instances are consecutive in the iteration order. If all
 * occurrences of an element are removed, after which that element is added to
 * the multiset, the element will appear at the end of the iteration.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial") // we're overriding default serialization
public final class LinkedHashMultiset<E> extends AbstractMapBasedMultiset<E> {

    /**
     * Creates a new, empty {@code LinkedHashMultiset} using the default initial
     * capacity.
     */
    public static <E> LinkedHashMultiset<E> create() {
        return new LinkedHashMultiset<E>();
    }

    /**
     * Creates a new, empty {@code LinkedHashMultiset} with the specified expected
     * number of distinct elements.
     *
     * @param distinctElements the expected number of distinct elements
     * @throws IllegalArgumentException if {@code distinctElements} is negative
     */
    public static <E> LinkedHashMultiset<E> create(int distinctElements) {
        return new LinkedHashMultiset<E>(distinctElements);
    }

    /**
     * Creates a new {@code LinkedHashMultiset} containing the specified elements.
     *
     * @param elements the elements that the multiset should contain
     */
    public static <E> LinkedHashMultiset<E> create(
            Iterable<? extends E> elements) {
        LinkedHashMultiset<E> multiset =
                create(Multisets.inferDistinctElements(elements));
        Iterables.addAll(multiset, elements);
        return multiset;
    }

    private LinkedHashMultiset() {
        super(new LinkedHashMap<E, AtomicInteger>());
    }

    private LinkedHashMultiset(int distinctElements) {
        // Could use newLinkedHashMapWithExpectedSize() if it existed
        super(new LinkedHashMap<E, AtomicInteger>(Maps.capacity(distinctElements)));
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
        setBackingMap(new LinkedHashMap<E, AtomicInteger>(
                Maps.capacity(distinctElements)));
        Serialization.populateMultiset(this, stream, distinctElements);
    }

    private static final long serialVersionUID = 0;
}
