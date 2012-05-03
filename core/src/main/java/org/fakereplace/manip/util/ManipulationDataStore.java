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

package org.fakereplace.manip.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fakereplace.com.google.common.collect.MapMaker;

/**
 * class that figures out which maniluation should be applied based on the
 * classloader of the relative classes.
 *
 * @param <T>
 * @author stuart
 */
public class ManipulationDataStore<T extends ClassLoaderFiltered<T>> {
    private final ClassLoader NULL_CLASS_LOADER = new ClassLoader() {
    };

    private final Map<ClassLoader, ConcurrentMap<String, Set<T>>> cldata = new MapMaker().weakKeys().makeComputingMap(new MapFunction<ClassLoader, String, Set<T>>(false));

    public Map<String, Set<T>> getManipulationData(ClassLoader loader) {
        if (loader == null) {
            loader = NULL_CLASS_LOADER;
        }
        Map<String, Set<T>> ret = new HashMap<String, Set<T>>();
        for (Entry<ClassLoader, ConcurrentMap<String, Set<T>>> centry : cldata.entrySet()) {
            for (Entry<String, Set<T>> e : centry.getValue().entrySet()) {
                Set<T> set = new HashSet<T>();
                ret.put(e.getKey(), set);
                for (ClassLoaderFiltered<T> f : e.getValue()) {
                    if (includeClassLoader(loader, f.getClassLoader())) {
                        set.add(f.getInstance());
                    }
                }
            }
        }

        return ret;
    }

    public void add(String name, T mdata) {
        ClassLoader loader = mdata.getClassLoader();
        if (loader == null) {
            loader = NULL_CLASS_LOADER;
        }
        ConcurrentMap<String, Set<T>> data = cldata.get(loader);
        Set<T> store = data.get(name);
        if(store == null) {
            store = new CopyOnWriteArraySet<T>();
            Set<T> existing = data.putIfAbsent(name, store);
            if(existing != null) {
                store = existing;
            }
        }
        store.add(mdata);
    }

    /**
     * even though it is tempting to just try
     * loaderOfClassBeingManipulated.loadClass(manipClassName) if this class
     * has not been loaded yet then this will cause problems, as this class will
     * not go through the agent. Instead we have
     * to try searching through the parent classloaders, which will not always
     * work.
     *
     * @param loaderOfClassBeingManipulated
     * @param loaderOfManipulatedClass
     * @return
     */
    public static boolean includeClassLoader(ClassLoader loaderOfClassBeingManipulated, ClassLoader loaderOfManipulatedClass) {
        if (loaderOfManipulatedClass == null) {
            return true;
        }
        ClassLoader loader = loaderOfClassBeingManipulated;
        while (loader != null) {
            if (loader == loaderOfManipulatedClass) {
                return true;
            }
            loader = loader.getParent();
        }
        return false;
    }

    @Override
    public String toString() {
        return "ManipulationDataStore{" +
                "cldata=" + cldata +
                '}';
    }

    public void remove(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = NULL_CLASS_LOADER;
        }
        Map<String, Set<T>> data = cldata.get(classLoader);
        if (data.containsKey(className)) {
            Set<T> set = data.get(className);
            Iterator<T> i = set.iterator();
            while (i.hasNext()) {
                T val = i.next();
                if (val.getClassLoader() == classLoader) {
                    i.remove();
                }
            }
        }
    }

    public Map<ClassLoader, ConcurrentMap<String, Set<T>>> getRawData() {
        return cldata;
    }


}
