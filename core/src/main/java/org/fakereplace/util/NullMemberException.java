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

import java.lang.reflect.Method;

/**
 * Exception thrown when a annotation is created with a null value
 * for one of the members.
 *
 * @author Stuart Douglas
 */
public class NullMemberException extends RuntimeException {
    Class annotationType;
    Method method;

    public NullMemberException(Class annotationType, Method method, String message) {
        super(message);
        this.annotationType = annotationType;
        this.method = method;
    }

    public Class getAnnotationType() {
        return annotationType;
    }

    public Method getMethod() {
        return method;
    }

}
