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

package org.fakereplace.com.google.common.base;

import org.fakereplace.com.google.common.annotations.GwtCompatible;


/**
 * A transformation from one object to another. For example, a
 * {@code StringToIntegerFunction} may implement
 * <code>Function&lt;String,Integer&gt;</code> and transform integers in
 * {@code String} format to {@code Integer} format.
 * <p/>
 * <p>The transformation on the source object does not necessarily result in
 * an object of a different type.  For example, a
 * {@code FarenheitToCelsiusFunction} may implement
 * <code>Function&lt;Float,Float&gt;</code>.
 * <p/>
 * <p>Implementations which may cause side effects upon evaluation are strongly
 * encouraged to state this fact clearly in their API documentation.
 *
 * @param <F> the type of the function input
 * @param <T> the type of the function output
 * @author Kevin Bourrillion
 * @author Scott Bonneau
 */
@GwtCompatible
public interface Function<F, T> {

    /**
     * Applies the function to an object of type {@code F}, resulting in an object
     * of type {@code T}.  Note that types {@code F} and {@code T} may or may not
     * be the same.
     *
     * @param from the source object
     * @return the resulting object
     */
    T apply(F from);

    /**
     * Indicates whether some other object is equal to this {@code Function}.
     * This method can return {@code true} <i>only</i> if the specified object is
     * also a {@code Function} and, for every input object {@code o}, it returns
     * exactly the same value.  Thus, {@code function1.equals(function2)} implies
     * that either {@code function1.apply(o)} and {@code function2.apply(o)} are
     * both null, or {@code function1.apply(o).equals(function2.apply(o))}.
     * <p/>
     * <p>Note that it is always safe <em>not</em> to override
     * {@link Object#equals}.
     */
    boolean equals(Object obj);
}
