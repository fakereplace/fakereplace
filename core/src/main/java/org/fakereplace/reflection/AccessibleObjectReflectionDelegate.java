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

import org.fakereplace.com.google.common.collect.MapMaker;
import sun.reflect.Reflection;

import java.lang.reflect.AccessibleObject;
import java.util.Map;

/**
 * tracks the accessible state of reflection items
 *
 * @author stuart
 */
public class AccessibleObjectReflectionDelegate {
    static Map<AccessibleObject, Boolean> accessibleMap = new MapMaker().weakKeys().makeMap();

    public static void setAccessible(AccessibleObject object, boolean accessible) {
        accessibleMap.put(object, accessible);
    }

    public static boolean isAccessible(AccessibleObject object) {
        Boolean res = accessibleMap.get(object);
        if (res == null) {
            return false;
        }
        return res;
    }

    /**
     * makes sure that a caller has permission to access an AccessibleObject and
     * calls setAccessible
     *
     * @param object
     * @param callerStackDepth
     */
    public static void ensureAccess(AccessibleObject object, int callerStackDepth, Class<?> declaringClass, int modifiers) throws IllegalAccessException {
        if (!isAccessible(object)) {
            Class<?> caller = sun.reflect.Reflection.getCallerClass(callerStackDepth);
            Reflection.ensureMemberAccess(caller, declaringClass, object, modifiers);
        }
        object.setAccessible(true);
    }
}
