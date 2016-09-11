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
