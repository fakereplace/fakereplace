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
import java.util.EnumMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.base.Preconditions;

/**
 * Multiset implementation backed by an {@link EnumMap}.
 *
 * @author Jared Levy
 */
@GwtCompatible
public final class EnumMultiset<E extends Enum<E>>
        extends AbstractMapBasedMultiset<E> {
    /**
     * Creates an empty {@code EnumMultiset}.
     */
    public static <E extends Enum<E>> EnumMultiset<E> create(Class<E> type) {
        return new EnumMultiset<E>(type);
    }

    /**
     * Creates a new {@code EnumMultiset} containing the specified elements.
     *
     * @param elements the elements that the multiset should contain
     * @throws IllegalArgumentException if {@code elements} is empty
     */
    public static <E extends Enum<E>> EnumMultiset<E> create(
            Iterable<E> elements) {
        Iterator<E> iterator = elements.iterator();
        Preconditions.checkArgument(iterator.hasNext(),
                "EnumMultiset constructor passed empty Iterable");
        EnumMultiset<E> multiset
                = new EnumMultiset<E>(iterator.next().getDeclaringClass());
        Iterables.addAll(multiset, elements);
        return multiset;
    }

    private transient Class<E> type;

    /**
     * Creates an empty {@code EnumMultiset}.
     */
    private EnumMultiset(Class<E> type) {
        super(new EnumMap<E, AtomicInteger>(type));
        this.type = type;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(type);
        Serialization.writeMultiset(this, stream);
    }

    /**
     * @serialData the {@code Class<E>} for the enum type, the number of distinct
     * elements, the first element, its count, the second element, its count,
     * and so on
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        @SuppressWarnings("unchecked") // reading data stored by writeObject
                Class<E> localType = (Class<E>) stream.readObject();
        type = localType;
        setBackingMap(new EnumMap<E, AtomicInteger>(type));
        Serialization.populateMultiset(this, stream);
    }

    private static final long serialVersionUID = 0;
}
