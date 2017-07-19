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

package org.fakereplace.integration.jsf;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.logging.Logger;
import org.jboss.el.cache.BeanPropertiesCache;

public class JSFClassChangeAware implements ClassChangeAware {

    private static final Logger log = Logger.getLogger(JSFClassChangeAware.class);

    private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (clazz == Object.class)
            throw new NoSuchFieldException();
        try {
            return clazz.getDeclaredField(name);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return getField(clazz.getSuperclass(), name);
    }

    @Override
    public void afterChange(List<ChangedClass> changed, List<NewClassData> addedOO) {
        try {
            Introspector.flushCaches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<?> data = InstanceTracker.get("javax.el.BeanELResolver");
        for (Object i : data) {
            clearBeanElResolver(i);
        }
        clearPropertiesCache();
    }

    private void clearPropertiesCache() {
        try {
            BeanPropertiesCache.SoftConcurrentHashMap properties = BeanPropertiesCache.getProperties();
            properties.clear();
            Field mapField = properties.getClass().getDeclaredField("map");
            mapField.setAccessible(true);
            Map map = (Map) mapField.get(properties);
            map.clear();
        } catch (Throwable e) {
            //class does not existing in older WF
        }
    }

    private void clearBeanElResolver(Object r) {
        try {
            try {
                Field cacheField = getField(r.getClass(), "cache");
                cacheField.setAccessible(true);
                Object cache = cacheField.get(r);
                try {
                    Method m = cache.getClass().getMethod("clear");
                    m.invoke(cache);
                } catch (NoSuchMethodException e) {
                    // different version of jboss el
                    Class<?> cacheClass = getClass().getClassLoader().loadClass("javax.el.BeanELResolver$ConcurrentCache");
                    Constructor<?> con = cacheClass.getConstructor(int.class);
                    con.setAccessible(true);
                    Object cacheInstance = con.newInstance(100);
                    cacheField.set(r, cacheInstance);
                }

            } catch (NoSuchFieldException ee) {
                try {
                    Field props = getField(r.getClass(), "properties");
                    props.setAccessible(true);
                    Object cache = props.get(r);
                    Method m = cache.getClass().getMethod("clear");
                    m.invoke(cache);
                } catch (NoSuchFieldException eee) {
                    //ignore
                }
            }
        } catch (Exception e) {
            log.error("Could not clear EL cache:", e);
        }
    }

}
