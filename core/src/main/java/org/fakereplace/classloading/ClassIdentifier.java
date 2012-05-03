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

/**
 *
 */
package org.fakereplace.classloading;

public class ClassIdentifier {
    private final String className;
    private final ClassLoader loader;

    public ClassIdentifier(String className, ClassLoader loader) {
        this.className = className;
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassIdentifier) {
            ClassIdentifier c = (ClassIdentifier) obj;
            return c.loader == loader && c.className.equals(className);
        }
        return false;
    }

}
