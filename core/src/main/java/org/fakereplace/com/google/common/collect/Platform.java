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

import java.lang.reflect.Array;
import java.util.List;

import org.fakereplace.com.google.common.annotations.GwtCompatible;
import org.fakereplace.com.google.common.annotations.GwtIncompatible;

/**
 * Methods factored out so that they can be emulated differently in GWT.
 *
 * @author Hayward Chan
 */
@GwtCompatible(emulated = true)
class Platform {

    /**
     * Calls {@link List#subList(int, int)}.  Factored out so that it can be
     * emulated in GWT.
     * <p/>
     * <p>This method is not supported in GWT yet.  See <a
     * href="http://code.google.com/p/google-web-toolkit/issues/detail?id=1791">
     * GWT issue 1791</a>
     */
    @GwtIncompatible("List.subList")
    static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Calls {@link Class#isInstance(Object)}.  Factored out so that it can be
     * emulated in GWT.
     */
    @GwtIncompatible("Class.isInstance")
    static boolean isInstance(Class<?> clazz, Object obj) {
        return clazz.isInstance(obj);
    }

    /**
     * Clone the given array using {@link Object#clone()}.  It is factored out so
     * that it can be emulated in GWT.
     */
    static <T> T[] clone(T[] array) {
        return array.clone();
    }

    /**
     * Returns a new array of the given length with the specified component type.
     *
     * @param type   the component type
     * @param length the length of the new array
     */
    @GwtIncompatible("Array.newInstance(Class, int)")
    @SuppressWarnings("unchecked")
    static <T> T[] newArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    /**
     * Returns a new array of the given length with the same type as a reference
     * array.
     *
     * @param reference any array of the desired type
     * @param length    the length of the new array
     */
    static <T> T[] newArray(T[] reference, int length) {
        Class<?> type = reference.getClass().getComponentType();

        // the cast is safe because
        // result.getClass() == reference.getClass().getComponentType()
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(type, length);
        return result;
    }

    private Platform() {
    }
}
