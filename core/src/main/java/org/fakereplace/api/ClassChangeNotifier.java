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

package org.fakereplace.api;

import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.com.google.common.collect.MapMaker;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassChangeNotifier {

    private static final ThreadLocal<Boolean> notificationInProgress = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };


    static Map<ClassLoader, Set<ClassChangeAware>> classChangeAwares = new MapMaker().weakKeys().makeMap();

    /**
     * These are objects that want to be notified but that do not have a
     * dependency on fakereplace.
     */
    static Map<ClassLoader, Set<Object>> unlinkedAwares = new MapMaker().weakKeys().makeMap();

    static public void add(ClassChangeAware aware) {
        if (!classChangeAwares.containsKey(aware.getClass().getClassLoader())) {
            classChangeAwares.put(aware.getClass().getClassLoader(), new HashSet<ClassChangeAware>());
        }
        classChangeAwares.get(aware.getClass().getClassLoader()).add(aware);
    }

    static public void add(Object aware) throws SecurityException, NoSuchMethodException {

        if (!unlinkedAwares.containsKey(aware.getClass().getClassLoader())) {
            unlinkedAwares.put(aware.getClass().getClassLoader(), new HashSet<Object>());
        }
        unlinkedAwares.get(aware.getClass().getClassLoader()).add(aware);
    }

    public static void notify(Class<?>[] changed, ClassIdentifier[] newClasses) {
        if (!notificationInProgress.get()) {
            notificationInProgress.set(true);
            try {
                Class<?>[] a = new Class[0];
                for (Set<ClassChangeAware> c : classChangeAwares.values()) {
                    for (ClassChangeAware i : c) {
                        try {
                            i.notify(changed, newClasses);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                for (Set<Object> c : unlinkedAwares.values()) {
                    for (Object i : c) {
                        try {
                            Method m = i.getClass().getMethod("notify", a.getClass(), a.getClass());
                            m.invoke(i, changed, newClasses);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                notificationInProgress.set(false);
            }
        }
    }

    public static void beforeChange(Class<?>[] changed, ClassIdentifier[] newClasses) {
        Class<?>[] a = new Class[0];
        for (Set<ClassChangeAware> c : classChangeAwares.values()) {
            for (ClassChangeAware i : c) {
                try {
                    i.beforeChange(changed, newClasses);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        for (Set<Object> c : unlinkedAwares.values()) {
            for (Object i : c) {
                try {
                    Method m = i.getClass().getMethod("beforeChange", a.getClass(), a.getClass());
                    m.invoke(i, changed, newClasses);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
