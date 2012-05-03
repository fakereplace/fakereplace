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

package org.fakereplace.manip.data;

import org.fakereplace.manip.util.ClassLoaderFiltered;

/**
 * Stores information about an added instance field.
 *
 * @author stuart
 */
public class AddedFieldData implements ClassLoaderFiltered<AddedFieldData> {
    private final int arrayIndex;
    private final String name;
    private final String descriptor;
    private final String className;
    private final ClassLoader classLoader;

    public AddedFieldData(int arrayIndex, String name, String descriptor, String className, ClassLoader classLoader) {
        super();
        this.arrayIndex = arrayIndex;
        this.name = name;
        this.descriptor = descriptor;
        this.className = className;
        this.classLoader = classLoader;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public AddedFieldData getInstance() {
        return this;
    }

}
