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

package org.fakereplace.manip.staticfield;

import java.lang.ref.WeakReference;

/**
 * Description of an added static field.
 *
 * @author stuart
 */
class StaticFieldDescriptor {
    private final WeakReference<Class<?>> clazz;
    private final String descriptor;
    private final String name;
    private final String signiture;

    public StaticFieldDescriptor(Class<?> clazz, String descriptor, String name, String signiture) {
        this.clazz = new WeakReference<Class<?>>(clazz);
        this.descriptor = descriptor;
        this.name = name;
        this.signiture = signiture;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        StaticFieldDescriptor other = (StaticFieldDescriptor) obj;
        if (descriptor == null) {
            if (other.descriptor != null)
                return false;
        } else if (!descriptor.equals(other.descriptor))
            return false;
        if (clazz == null || clazz.get() == null) {
            if (other.clazz != null || other.clazz.get() != null)
                return false;
        } else if (!clazz.get().equals(other.clazz.get()))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (signiture == null) {
            if (other.signiture != null) {
                return false;
            }
        } else if (!signiture.equals(other.signiture)) {
            return false;
        }
        return true;
    }

    public Class<?> getClazz() {
        return clazz.get();
    }

    public String getSigniture() {
        return signiture;
    }
}
