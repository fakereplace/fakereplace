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

package org.fakereplace.replacement;

import org.fakereplace.classloading.ClassIdentifier;

public class AddedClass {
    private final String className;
    private final byte[] data;
    private final ClassLoader loader;

    public AddedClass(String className, byte[] data, ClassLoader loader) {
        this.className = className;
        this.data = data;
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    public byte[] getData() {
        return data;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public ClassIdentifier getClassIdentifier() {
        return new ClassIdentifier(className, loader);
    }
}
