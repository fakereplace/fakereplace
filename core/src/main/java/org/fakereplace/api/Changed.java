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

/**
 * Represents an addition, a removal or a modification
 *
 * @author Stuart Douglas
 */
public class Changed<T> {

    private final ChangeType type;
    private final T modified;
    private final T existing;

    public Changed(T modified, T existing, ChangeType type) {
        this.modified = modified;
        this.existing = existing;
        this.type = type;
    }

    public ChangeType getType() {
        return type;
    }

    public T getModified() {
        return modified;
    }

    public T getExisting() {
        return existing;
    }
}
