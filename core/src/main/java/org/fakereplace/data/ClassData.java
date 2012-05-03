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

package org.fakereplace.data;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.util.DescriptorUtils;

/**
 * This class holds everything there is to know about a class that has been seen
 * by the transformer
 *
 * @author stuart
 */
public class ClassData {

    private final String className;
    private final String internalName;
    private final Map<String, Map<String, Set<MethodData>>> methods = new MapMaker().makeMap();
    private final Map<Method, MethodData> methodsByMethod = new MapMaker().makeComputingMap(new MethodResolver());
    private final Map<String, FieldData> fields = new MapMaker().makeMap();
    private final Set<MethodData> methodSet = new HashSet<MethodData>();
    private final ClassLoader loader;
    private final String superClassName;
    private final boolean signitureModified;
    private final boolean replaceable;

    private static final MethodData NULL_METHOD_DATA = new MethodData("", "", "", null, 0, false);

    ClassData(BaseClassData data, Set<MethodData> addMethods, Set<MethodData> removedMethods, Set<FieldData> addedFields, Set<FieldData> removedFields) {
        className = data.getClassName();
        internalName = data.getInternalName();
        loader = data.getLoader();
        superClassName = data.getSuperClassName();
        signitureModified = removedFields.isEmpty() && removedMethods.isEmpty() && addedFields.isEmpty() && addMethods.isEmpty();
        replaceable = data.isReplaceable();
        for (MethodData m : data.getMethods()) {
            if (!removedMethods.contains(m)) {
                addMethod(m);
            }
        }
        for (FieldData f : data.getFields()) {
            if (!removedFields.contains(f)) {
                addField(f);
            }
        }

        for (FieldData f : removedFields) {
            addField(f);
        }
        for (FieldData f : addedFields) {
            addField(f);
        }

        for (MethodData m : removedMethods) {
            addMethod(m);
        }

        for (MethodData m : addMethods) {
            addMethod(m);
        }

        for (String nm : methods.keySet()) {
            for (String i : methods.get(nm).keySet()) {
                methodSet.addAll(methods.get(nm).get(i));
            }
        }
    }

    public MethodData getData(Method method) {
        MethodData res = methodsByMethod.get(method);
        if (res == NULL_METHOD_DATA) {
            return null;
        }
        return res;
    }

    ClassData(BaseClassData data) {
        className = data.getClassName();
        internalName = data.getInternalName();
        loader = data.getLoader();
        superClassName = data.getSuperClassName();
        replaceable = data.isReplaceable();
        for (MethodData m : data.getMethods()) {
            addMethod(m);
        }
        for (FieldData f : data.getFields()) {
            addField(f);
        }
        signitureModified = false;
    }

    public boolean isSignitureModified() {
        return signitureModified;
    }

    /**
     * Searches through parent classloaders of the classes class loader to find
     * the ClassData structure for the super class
     *
     * @return
     */
    public ClassData getSuperClassInformation() {
        if (superClassName == null) {
            return null;
        }
        ClassData superClassInformation = ClassDataStore.instance().getModifiedClassData(loader, superClassName);
        ClassLoader l = loader;
        while (superClassInformation == null && l != null) {
            l = l.getParent();
            superClassInformation = ClassDataStore.instance().getModifiedClassData(l, superClassName);
        }
        return superClassInformation;
    }

    public FieldData getField(String field) {
        return fields.get(field);
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public String getClassName() {
        return className;
    }

    public String getInternalName() {
        return internalName;
    }

    public void addMethod(MethodData data) {

        if (!methods.containsKey(data.getMethodName())) {
            methods.put(data.getMethodName(), new HashMap<String, Set<MethodData>>());
        }
        Map<String, Set<MethodData>> mts = methods.get(data.getMethodName());
        if (!mts.containsKey(data.getArgumentDescriptor())) {
            mts.put(data.getArgumentDescriptor(), new HashSet<MethodData>());
        }
        Set<MethodData> rr = mts.get(data.getArgumentDescriptor());
        rr.add(data);

    }

    /**
     * replaces a method if it already exists
     *
     * @param data
     */
    public void replaceMethod(MethodData data) {
        if (!methods.containsKey(data.getMethodName())) {
            methods.put(data.getMethodName(), new HashMap<String, Set<MethodData>>());
        }
        Map<String, Set<MethodData>> mts = methods.get(data.getMethodName());
        Set<MethodData> rr = new HashSet<MethodData>();
        mts.put(data.getArgumentDescriptor(), rr);
        rr.add(data);

    }

    public void addField(FieldData data) {
        fields.put(data.getName(), data);
    }

    public Collection<MethodData> getMethods() {
        return methodSet;
    }

    public Collection<FieldData> getFields() {
        return fields.values();
    }

    /**
     * gets the method data based on name and signature. If there is multiple
     * methods with the same name and signature it is undefined which one will be
     * returned
     *
     * @param name
     * @param arguments
     * @return
     */
    public MethodData getMethodData(String name, String arguments) {
        Map<String, Set<MethodData>> r = methods.get(name);
        if (r == null) {
            return null;
        }
        Set<MethodData> ms = r.get(arguments);

        if (ms == null) {
            return null;
        }
        return ms.iterator().next();
    }

    private static class MethodResolver implements Function<Method, MethodData> {

        public MethodData apply(Method from) {
            ClassData dta = ClassDataStore.instance().getModifiedClassData(from.getDeclaringClass().getClassLoader(), from.getDeclaringClass().getName());
            if (dta == null) {
                return NULL_METHOD_DATA;
            }
            String descriptor = DescriptorUtils.getDescriptor(from);
            for (MethodData m : dta.getMethods()) {
                if (m.getMethodName().equals(from.getName()) && descriptor.equals(m.getDescriptor())) {
                    return m;
                }
            }
            return NULL_METHOD_DATA;

        }

    }

    public boolean isReplaceable() {
        return replaceable;
    }
}
