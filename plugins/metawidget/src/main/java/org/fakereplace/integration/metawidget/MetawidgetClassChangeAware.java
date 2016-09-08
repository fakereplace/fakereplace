/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
