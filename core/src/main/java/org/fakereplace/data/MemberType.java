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

package org.fakereplace.data;

public enum MemberType {
    /**
     * normal methods are methods from the java source code
     */
    NORMAL,
    /**
     * fake methods are methods that we are pretending exist
     */
    FAKE,

    FAKE_CONSTRUCTOR,
    /**
     * This is a method that we have to implement with a noop as it was removed
     * from the source
     */
    REMOVED,
    /**
     * This is a method that has been added that should not be visible to the
     * user
     */
    ADDED_SYSTEM;
}
