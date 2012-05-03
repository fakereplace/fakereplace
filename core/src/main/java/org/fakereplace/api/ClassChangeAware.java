/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.api;

import org.fakereplace.classloading.ClassIdentifier;

/**
 * interface that should be implemented by classes that with to be notified of
 * class changes.
 *
 * The classes that implement this interface are loaded into the
 *
 * @author stuart
 */
public interface ClassChangeAware {
    void beforeChange(Class<?>[] changed, ClassIdentifier[] added);

    void notify(Class<?>[] changed, ClassIdentifier[] added);
}
