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

import java.util.Map;


/**
 * A map, each entry of which maps a Java
 * <a href="http://tinyurl.com/2cmwkz">raw type</a> to an instance of that type.
 * In addition to implementing {@code Map}, the additional type-safe operations
 * {@link #putInstance} and {@link #getInstance} are available.
 * <p/>
 * <p>Like any other {@code Map<Class, Object>}, this map may contain entries
 * for primitive types, and a primitive type and its corresponding wrapper type
 * may map to different values.
 *
 * @param <B> the common supertype that all entries must share; often this is
 *            simply {@link Object}
 * @author Kevin Bourrillion
 */
@GwtCompatible
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B> {
    /**
     * Returns the value the specified class is mapped to, or {@code null} if no
     * entry for this class is present. This will only return a value that was
     * bound to this specific class, not a value that may have been bound to a
     * subtype.
     */
    <T extends B> T getInstance(Class<T> type);

    /**
     * Maps the specified class to the specified value. Does <i>not</i> associate
     * this value with any of the class's supertypes.
     *
     * @return the value previously associated with this class (possibly {@code
     *         null}), or {@code null} if there was no previous entry.
     */
    <T extends B> T putInstance(Class<T> type, T value);
}
