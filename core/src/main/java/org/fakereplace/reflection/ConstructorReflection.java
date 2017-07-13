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

package org.fakereplace.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.fakereplace.core.Constants;
import org.fakereplace.core.ConstructorArgument;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.util.DescriptorUtils;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;

public class ConstructorReflection {

    @SuppressWarnings("restriction")
    public static Object newInstance(Constructor<?> method, Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final MethodData data = ClassDataStore.instance().getMethodInformation(method.getDeclaringClass().getName());
        final Class<?> info = ClassDataStore.instance().getRealClassFromProxyName(method.getDeclaringClass().getName());
        try {
            final Constructor<?> invoke = info.getConstructor(int.class, Object[].class, ConstructorArgument.class);
            Object ar = args;
            if (ar == null) {
                ar = new Object[0];
            }
            if (!Modifier.isPublic(method.getModifiers()) && !method.isAccessible()) {
                Class<?> caller = AccessVerification.getCallerClass(2);
                AccessVerification.ensureMemberAccess(caller, method.getDeclaringClass(), method.getModifiers());
            }
            return invoke.newInstance(data.getMethodNo(), ar, null);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) {
        try {
            ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

            if (cd == null || !cd.isReplaceable()) {
                return clazz.getDeclaredConstructors();
            }
            Constructor<?>[] meth = clazz.getDeclaredConstructors();
            List<Constructor<?>> visible = new ArrayList<>(meth.length);
            for (int i = 0; i < meth.length; ++i) {
                if (meth[i].getParameterTypes().length != 3 || !meth[i].getParameterTypes()[2].equals(ConstructorArgument.class)) {
                    visible.add(meth[i]);
                }
            }

            for (MethodData i : cd.getMethods()) {
                if (i.getType() == MemberType.FAKE_CONSTRUCTOR) {
                    Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                    visible.add(i.getConstructor(c));
                } else if (i.getType() == MemberType.REMOVED && i.getMethodName().equals("<init>")) {
                    Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                    visible.remove(i.getConstructor(c));
                }
            }

            Constructor<?>[] ret = new Constructor[visible.size()];
            for (int i = 0; i < visible.size(); ++i) {
                ret[i] = visible.get(i);
            }

            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?>[] getConstructors(Class<?> clazz) {
        try {
            ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

            if (cd == null || !cd.isReplaceable()) {
                return clazz.getConstructors();
            }

            Constructor<?>[] meth = clazz.getConstructors();
            List<Constructor<?>> visible = new ArrayList<>(meth.length);
            for (int i = 0; i < meth.length; ++i) {
                if (meth[i].getParameterTypes().length != 3 || !meth[i].getParameterTypes()[2].equals(ConstructorArgument.class)) {
                    visible.add(meth[i]);
                }
            }

            ClassData cta = cd;
            while (cta != null) {
                for (MethodData i : cta.getMethods()) {
                    if (i.isConstructor()) {
                        if (i.getType() == MemberType.FAKE_CONSTRUCTOR && AccessFlag.isPublic(i.getAccessFlags())) {
                            Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                            visible.add(i.getConstructor(c));
                        } else if (i.getType() == MemberType.REMOVED && i.getMethodName().equals("<init>")) {
                            Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                            visible.remove(i.getConstructor(c));
                        }
                    }
                }
                cta = cta.getSuperClassInformation();
            }

            Constructor<?>[] ret = new Constructor[visible.size()];
            for (int i = 0; i < visible.size(); ++i) {
                ret[i] = visible.get(i);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) throws NoSuchMethodException {

        ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

        if (cd == null || !cd.isReplaceable()) {
            return clazz.getConstructor(parameters);
        }
        String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
        MethodData md = cd.getMethodData("<init>", args);
        if (md == null) {
            return clazz.getConstructor(parameters);
        }

        switch (md.getType()) {
            case NORMAL:
                return clazz.getConstructor(parameters);
            case FAKE_CONSTRUCTOR:
                try {
                    Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
                    return c.getConstructor(parameters);
                } catch (NoSuchMethodException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
        throw new NoSuchMethodException();
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> clazz, Class<?>... parameters) throws NoSuchMethodException {

        ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

        if (cd == null || !cd.isReplaceable()) {
            return clazz.getDeclaredConstructor(parameters);
        }
        String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
        MethodData md = cd.getMethodData("<init>", args);
        if (md == null) {
            return clazz.getDeclaredConstructor(parameters);
        }

        switch (md.getType()) {
            case NORMAL:
                return clazz.getDeclaredConstructor(parameters);
            case FAKE_CONSTRUCTOR:
                try {
                    Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
                    return c.getDeclaredConstructor(parameters);
                } catch (NoSuchMethodException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
        throw new NoSuchMethodException();
    }

    public static Class<?> getDeclaringClass(Constructor<?> f) {
        Class<?> c = f.getDeclaringClass();
        if (c.getName().startsWith(Constants.GENERATED_CLASS_PACKAGE)) {
            return ClassDataStore.instance().getRealClassFromProxyName(c.getName());
        }
        return c;
    }

    public static boolean fakeCallRequired(Constructor<?> method) {
        return method.getDeclaringClass().getName().startsWith(Constants.GENERATED_CLASS_PACKAGE);
    }
}
