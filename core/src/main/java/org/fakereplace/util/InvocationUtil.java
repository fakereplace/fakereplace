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

package org.fakereplace.util;

/**
 * @author stuart
 */
public class InvocationUtil {
    /**
     * appends object to the start of the array
     */
    static public Object[] prepare(Object object, Object[] array) {
        int length = 0;
        if (array != null) {
            length = array.length;
        }
        Object[] ret = new Object[length + 1];
        ret[0] = object;
        for (int i = 0; i < length; ++i) {
            ret[i + 1] = array[i];
        }
        return ret;
    }

}
