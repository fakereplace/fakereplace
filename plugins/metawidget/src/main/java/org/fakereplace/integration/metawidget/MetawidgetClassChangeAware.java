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

package org.fakereplace.integration.metawidget;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.InstanceTracker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetawidgetClassChangeAware implements ClassChangeAware {

    private static Method remove;

    static {
        try {
            remove = Map.class.getMethod("remove", Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (clazz == Object.class)
            throw new NoSuchFieldException();
        try {
            return clazz.getDeclaredField(name);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return getField(clazz.getSuperclass(), name);
    }

    /**
     * clear the action and properties caches
     */
    @Override
    public void afterChange(List<ChangedClass> changed, List<NewClassData> added) {
        Set<?> data = InstanceTracker.get(MetawidgetExtension.BASE_ACTION_STYLE);
        for (Object i : data) {
            clearMap(changed, i, "mActionCache");
        }
        data = InstanceTracker.get(MetawidgetExtension.BASE_PROPERTY_STYLE);
        for (Object i : data) {
            clearMap(changed, i, "mPropertiesCache");
        }

    }

    public static void clearMap(List<ChangedClass> changed, Object i, String cacheName) {
        try {
            Field f = getField(i.getClass(), cacheName);
            f.setAccessible(true);
            Object map = f.get(i);
            for (ChangedClass c : changed) {
                remove.invoke(map, c.getChangedClass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
