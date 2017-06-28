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

package org.fakereplace.data;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.fakereplace.util.DescriptorUtils;

/**
 * This class holds everything there is to know about a class that has been seen
 * by the transformer
 *
 * @author stuart
 */
public class ClassData {

    private static final MethodData NULL_METHOD_DATA = new MethodData("", "", "", null, 0, false);

    private static Function<Method, MethodData> METHOD_RESOLVER = from -> {
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
    };

    private final String className;
    private final String internalName;
    private final Map<String, Map<String, Set<MethodData>>> methods = Collections.synchronizedMap(new HashMap<>());
    private final Map<Method, MethodData> methodsByMethod = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, FieldData> fields = Collections.synchronizedMap(new HashMap<>());
    private final Set<MethodData> methodSet = new HashSet<MethodData>();
    private final ClassLoader loader;
    private final String superClassName;
    private final boolean signitureModified;
    private final boolean replaceable;


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
        MethodData res = methodsByMethod.computeIfAbsent(method, METHOD_RESOLVER);
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


    public boolean isReplaceable() {
        return replaceable;
    }
}
