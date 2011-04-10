package org.fakereplace.api;

import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.com.google.common.collect.MapMaker;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassChangeNotifier {
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
        Class<?>[] a = new Class[0];
        for (Set<ClassChangeAware> c : classChangeAwares.values()) {
            for (ClassChangeAware i : c) {
                i.notify(changed, newClasses);
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
