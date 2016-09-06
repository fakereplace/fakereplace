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

package org.fakereplace.replacement.notification;

import org.fakereplace.api.ChangedClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds builders for changed class information as the classes are being hot replaced.
 *
 * After the replacement this information is provided to the integrations.
 *
 * @author Stuart Douglas
 */
public class CurrentChangedClasses {

    private static final ThreadLocal<Map<Class<?>, ChangedClassImpl>> CHANGED = new ThreadLocal<Map<Class<?>, ChangedClassImpl>>();

    public static ChangedClassImpl get(final Class<?> clazz) {
        Map<Class<?>, ChangedClassImpl> currentchanged = CHANGED.get();
        if(currentchanged == null) {
            return null;
        }
        return currentchanged.get(clazz);
    }

    public static void prepareClasses(final List<Class<?>> changed) {
        final HashMap<Class<?>, ChangedClassImpl> map = new HashMap<Class<?>, ChangedClassImpl>();
        CHANGED.set(map);
        for(Class<?> clazz: changed) {
            map.put(clazz, new ChangedClassImpl(clazz));
        }
    }

    public static List<ChangedClass> getChanged() {
        final List<ChangedClass> ret = new ArrayList<ChangedClass>(CHANGED.get().values());
        CHANGED.remove();
        return ret;
    }


}
