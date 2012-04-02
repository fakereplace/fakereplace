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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.base.Preconditions;


/**
 * An immutable collection. Does not permit null elements.
 * <p/>
 * <p><b>Note</b>: Although this class is not final, it cannot be subclassed
 * outside of this package as it has no public or protected constructors. Thus,
 * instances of this type are guaranteed to be immutable.
 *
 * @author Jesse Wilson
 */
@GwtCompatible
@SuppressWarnings("serial") // we're overriding default serialization
public abstract class ImmutableCollection<E>
        implements Collection<E>, Serializable {
    static final ImmutableCollection<Object> EMPTY_IMMUTABLE_COLLECTION
            = new EmptyImmutableCollection();

    ImmutableCollection() {
    }

    /**
     * Returns an unmodifiable iterator across the elements in this collection.
     */
    public abstract UnmodifiableIterator<E> iterator();

    public Object[] toArray() {
        Object[] newArray = new Object[size()];
        return toArray(newArray);
    }

    public <T> T[] toArray(T[] other) {
        int size = size();
        if (other.length < size) {
            other = ObjectArrays.newArray(other, size);
        } else if (other.length > size) {
            other[size] = null;
        }

        // Writes will produce ArrayStoreException when the toArray() doc requires.
        Object[] otherAsObjectArray = other;
        int index = 0;
        for (E element : this) {
            otherAsObjectArray[index++] = element;
        }
        return other;
    }

    public boolean contains(Object object) {
        if (object == null) {
            return false;
        }
        for (E element : this) {
            if (element.equals(object)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> targets) {
        for (Object target : targets) {
            if (!contains(target)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size() * 16).append('[');
        Collections2.standardJoiner.appendTo(sb, this);
        return sb.append(']').toString();
    }

    /**
     * Guaranteed to throw an exception and leave the collection unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the collection unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the collection unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final boolean addAll(Collection<? extends E> newElements) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the collection unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final boolean removeAll(Collection<?> oldElements) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the collection unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final boolean retainAll(Collection<?> elementsToKeep) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the collection unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    private static class EmptyImmutableCollection
            extends ImmutableCollection<Object> {
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object object) {
            return false;
        }

        @Override
        public UnmodifiableIterator<Object> iterator() {
            return Iterators.EMPTY_ITERATOR;
        }

        private static final Object[] EMPTY_ARRAY = new Object[0];

        @Override
        public Object[] toArray() {
            return EMPTY_ARRAY;
        }

        @Override
        public <T> T[] toArray(T[] array) {
            if (array.length > 0) {
                array[0] = null;
            }
            return array;
        }
    }

    private static class ArrayImmutableCollection<E>
            extends ImmutableCollection<E> {
        private final E[] elements;

        ArrayImmutableCollection(E[] elements) {
            this.elements = elements;
        }

        public int size() {
            return elements.length;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public UnmodifiableIterator<E> iterator() {
            return Iterators.forArray(elements);
        }
    }

    /*
    * Serializes ImmutableCollections as their logical contents. This ensures
    * that implementation types do not leak into the serialized representation.
    */
    private static class SerializedForm implements Serializable {
        final Object[] elements;

        SerializedForm(Object[] elements) {
            this.elements = elements;
        }

        Object readResolve() {
            return elements.length == 0
                    ? EMPTY_IMMUTABLE_COLLECTION
                    : new ArrayImmutableCollection<Object>(Platform.clone(elements));
        }

        private static final long serialVersionUID = 0;
    }

    Object writeReplace() {
        return new SerializedForm(toArray());
    }

    /**
     * Abstract base class for builders of {@link ImmutableCollection} types.
     */
    abstract static class Builder<E> {
        /**
         * Adds {@code element} to the {@code ImmutableCollection} being built.
         * <p/>
         * <p>Note that each builder class covariantly returns its own type from
         * this method.
         *
         * @param element the element to add
         * @return this {@code Builder} instance
         * @throws NullPointerException if {@code element} is null
         */
        public abstract Builder<E> add(E element);

        /**
         * Adds each element of {@code elements} to the {@code ImmutableCollection}
         * being built.
         * <p/>
         * <p>Note that each builder class overrides this method in order to
         * covariantly return its own type.
         *
         * @param elements the elements to add
         * @return this {@code Builder} instance
         * @throws NullPointerException if {@code elements} is null or contains a
         *                              null element
         */
        public Builder<E> add(E... elements) {
            Preconditions.checkNotNull(elements); // for GWT
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        /**
         * Adds each element of {@code elements} to the {@code ImmutableCollection}
         * being built.
         * <p/>
         * <p>Note that each builder class overrides this method in order to
         * covariantly return its own type.
         *
         * @param elements the elements to add
         * @return this {@code Builder} instance
         * @throws NullPointerException if {@code elements} is null or contains a
         *                              null element
         */
        public Builder<E> addAll(Iterable<? extends E> elements) {
            Preconditions.checkNotNull(elements); // for GWT
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        /**
         * Adds each element of {@code elements} to the {@code ImmutableCollection}
         * being built.
         * <p/>
         * <p>Note that each builder class overrides this method in order to
         * covariantly return its own type.
         *
         * @param elements the elements to add
         * @return this {@code Builder} instance
         * @throws NullPointerException if {@code elements} is null or contains a
         *                              null element
         */
        public Builder<E> addAll(Iterator<? extends E> elements) {
            Preconditions.checkNotNull(elements); // for GWT
            while (elements.hasNext()) {
                add(elements.next());
            }
            return this;
        }

        /**
         * Returns a newly-created {@code ImmutableCollection} of the appropriate
         * type, containing the elements provided to this builder.
         * <p/>
         * <p>Note that each builder class covariantly returns the appropriate type
         * of {@code ImmutableCollection} from this method.
         */
        public abstract ImmutableCollection<E> build();
    }
}
