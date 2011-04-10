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

/**
 * "Overrides" the {@link ImmutableSet} static methods that lack
 * {@link ImmutableSortedSet} equivalents with deprecated, exception-throwing
 * versions. This prevents accidents like the following:<pre>   {@code
 * <p/>
 *   List<Object> objects = ...;
 *   // Sort them:
 *   Set<Object> sorted = ImmutableSortedSet.copyOf(objects);
 *   // BAD CODE! The returned set is actually an unsorted ImmutableSet!}</pre>
 * <p/>
 * <p>While we could put the overrides in {@link ImmutableSortedSet} itself, it
 * seems clearer to separate these "do not call" methods from those intended for
 * normal use.
 *
 * @author Chris Povirk
 */
@GwtCompatible
abstract class ImmutableSortedSetFauxverideShim<E> extends ImmutableSet<E> {
    /**
     * Not supported. Use {@link ImmutableSortedSet#naturalOrder}, which offers
     * better type-safety, instead. This method exists only to hide
     * {@link ImmutableSet#builder} from consumers of {@code ImmutableSortedSet}.
     *
     * @throws UnsupportedOperationException always
     * @deprecated Use {@link ImmutableSortedSet#naturalOrder}, which offers
     *             better type-safety.
     */
    @Deprecated
    public static <E> ImmutableSortedSet.Builder<E> builder() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. <b>You are attempting to create a set that may contain a
     * non-{@code Comparable} element.</b> Proper calls will resolve to the
     * version in {@code ImmutableSortedSet}, not this dummy version.
     *
     * @throws UnsupportedOperationException always
     * @deprecated <b>Pass a parameter of type {@code Comparable} to use {@link
     *             ImmutableSortedSet#of(Comparable)}.</b>
     */
    @Deprecated
    public static <E> ImmutableSortedSet<E> of(E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. <b>You are attempting to create a set that may contain a
     * non-{@code Comparable} element.</b> Proper calls will resolve to the
     * version in {@code ImmutableSortedSet}, not this dummy version.
     *
     * @throws UnsupportedOperationException always
     * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
     *             ImmutableSortedSet#of(Comparable, Comparable)}.</b>
     */
    @Deprecated
    public static <E> ImmutableSortedSet<E> of(E e1, E e2) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. <b>You are attempting to create a set that may contain a
     * non-{@code Comparable} element.</b> Proper calls will resolve to the
     * version in {@code ImmutableSortedSet}, not this dummy version.
     *
     * @throws UnsupportedOperationException always
     * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
     *             ImmutableSortedSet#of(Comparable, Comparable, Comparable)}.</b>
     */
    @Deprecated
    public static <E> ImmutableSortedSet<E> of(E e1, E e2, E e3) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. <b>You are attempting to create a set that may contain a
     * non-{@code Comparable} element.</b> Proper calls will resolve to the
     * version in {@code ImmutableSortedSet}, not this dummy version.
     *
     * @throws UnsupportedOperationException always
     * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
     *             ImmutableSortedSet#of(Comparable, Comparable, Comparable, Comparable)}.
     *             </b>
     */
    @Deprecated
    public static <E> ImmutableSortedSet<E> of(
            E e1, E e2, E e3, E e4) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. <b>You are attempting to create a set that may contain a
     * non-{@code Comparable} element.</b> Proper calls will resolve to the
     * version in {@code ImmutableSortedSet}, not this dummy version.
     *
     * @throws UnsupportedOperationException always
     * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
     *             ImmutableSortedSet#of(
     *Comparable, Comparable, Comparable, Comparable, Comparable)}. </b>
     */
    @Deprecated
    public static <E> ImmutableSortedSet<E> of(
            E e1, E e2, E e3, E e4, E e5) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported. <b>You are attempting to create a set that may contain
     * non-{@code Comparable} elements.</b> Proper calls will resolve to the
     * version in {@code ImmutableSortedSet}, not this dummy version.
     *
     * @throws UnsupportedOperationException always
     * @deprecated <b>Pass parameters of type {@code Comparable} to use {@link
     *             ImmutableSortedSet#of(Comparable[])}.</b>
     */
    @Deprecated
    public static <E> ImmutableSortedSet<E> of(E... elements) {
        throw new UnsupportedOperationException();
    }

    /*
    * We would like to include an unsupported "<E> copyOf(Iterable<E>)" here,
    * providing only the properly typed
    * "<E extends Comparable<E>> copyOf(Iterable<E>)" in ImmutableSortedSet (and
    * likewise for the Iterator equivalent). However, due to a change in Sun's
    * interpretation of the JLS (as described at
    * http://bugs.sun.com/view_bug.do?bug_id=6182950), the OpenJDK 7 compiler
    * available as of this writing rejects our attempts. To maintain
    * compatibility with that version and with any other compilers that interpret
    * the JLS similarly, there is no definition of copyOf() here, and the
    * definition in ImmutableSortedSet matches that in ImmutableSet.
    *
    * The result is that ImmutableSortedSet.copyOf() may be called on
    * non-Comparable elements. We have not discovered a better solution. In
    * retrospect, the static factory methods should have gone in a separate class
    * so that ImmutableSortedSet wouldn't "inherit" too-permissive factory
    * methods from ImmutableSet.
    */
}
