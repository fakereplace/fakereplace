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
 * Determines a true or false value for a given input. For example, a
 * {@code RegexPredicate} might implement {@code Predicate<String>}, and return
 * {@code true} for any string that matches its given regular expression.
 * <p/>
 * <p>Implementations which may cause side effects upon evaluation are strongly
 * encouraged to state this fact clearly in their API documentation.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible
public interface Predicate<T> {

    /*
    * This interface does not extend Function<T, Boolean> because doing so would
    * let predicates return null.
    */

    /**
     * Applies this predicate to the given object.
     *
     * @param input the input that the predicate should act on
     * @return the value of this predicate when applied to the input {@code t}
     */
    boolean apply(T input);

    /**
     * Indicates whether some other object is equal to this {@code Predicate}.
     * This method can return {@code true} <i>only</i> if the specified object is
     * also a {@code Predicate} and, for every input object {@code input}, it
     * returns exactly the same value. Thus, {@code predicate1.equals(predicate2)}
     * implies that either {@code predicate1.apply(input)} and
     * {@code predicate2.apply(input)} are both {@code true} or both
     * {@code false}.
     * <p/>
     * <p>Note that it is always safe <i>not</i> to override
     * {@link Object#equals}.
     */
    boolean equals(Object obj);
}
