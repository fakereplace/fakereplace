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

package org.fakereplace.integration.jsf;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.data.InstanceTracker;

public class ClassRedefinitionPlugin implements ClassChangeAware {

    Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (clazz == Object.class)
            throw new NoSuchFieldException();
        try {
            return clazz.getDeclaredField(name);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return getField(clazz.getSuperclass(), name);
    }

    public void beforeChange(Class<?>[] changed, ClassIdentifier[] added) {

    }

    public void notify(Class<?>[] changed, ClassIdentifier[] added) {
        try {
            Introspector.flushCaches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<?> data = InstanceTracker.get("javax.el.BeanELResolver");
        for (Object i : data) {
            clearBeanElResolver(i);
        }
    }

    public void clearBeanElResolver(Object r) {
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
                Field props = getField(r.getClass(), "properties");
                props.setAccessible(true);
                Object cache = props.get(r);
                Method m = cache.getClass().getMethod("clear");
                m.invoke(cache);
            }
        } catch (Exception e) {
            System.out.println("Could not clear EL cache:" + e.getMessage());
        }
    }

}
