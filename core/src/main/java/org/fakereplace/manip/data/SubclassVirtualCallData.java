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

public class SubclassVirtualCallData implements ClassLoaderFiltered<SubclassVirtualCallData> {

    private final ClassLoader classLoader;
    private final String className;
    private final String methodName;
    private final String methodDesc;

    public SubclassVirtualCallData(ClassLoader classLoader, String className, String methodName, String methodDesc) {
        this.classLoader = classLoader;
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    public SubclassVirtualCallData getInstance() {
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((methodDesc == null) ? 0 : methodDesc.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubclassVirtualCallData other = (SubclassVirtualCallData) obj;
        if (classLoader == null) {
            if (other.classLoader != null)
                return false;
        } else if (!classLoader.equals(other.classLoader))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (methodDesc == null) {
            if (other.methodDesc != null)
                return false;
        } else if (!methodDesc.equals(other.methodDesc))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

}
