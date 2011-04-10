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

package org.fakereplace.reflection;

import org.fakereplace.data.FieldDataStore;

/**
 * Class that knows how to set and get replaced fields. This is very inefficent,
 * and should be replaced with bytecode generation based reflection
 *
 * @author stuart
 */
public class FieldAccessor {

    final Class<?> clazz;
    final Integer mapKey;

    public FieldAccessor(Class<?> clazz, int mapKey) {
        this.clazz = clazz;
        this.mapKey = mapKey;
    }

    public void set(Object object, Object value) {
        FieldDataStore.setValue(object, value, mapKey);
    }

    public Object get(Object object) {
        return FieldDataStore.getValue(object, mapKey);
    }

}
