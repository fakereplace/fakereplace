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

package org.fakereplace.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;
import org.fakereplace.core.Constants;
import org.fakereplace.core.ConstructorArgument;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.util.DescriptorUtils;
import sun.reflect.Reflection;

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
                Class<?> caller = sun.reflect.Reflection.getCallerClass(2);
                Reflection.ensureMemberAccess(caller, method.getDeclaringClass(), null, method.getModifiers());
            }
            return invoke.newInstance(data.getMethodNo(), ar, null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
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
            List<Constructor<?>> visible = new ArrayList<Constructor<?>>(meth.length);
            for (int i = 0; i < meth.length; ++i) {
                if (meth[i].getParameterTypes().length != 3 || !meth[i].getParameterTypes()[2].equals(ConstructorArgument.class)) {
                    visible.add(meth[i]);
                }
            }

            for (MethodData i : cd.getMethods()) {
                if (i.getType() == MemberType.FAKE_CONSTRUCTOR) {
                    Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                    visible.add(i.getConstructor(c));
                } else if (i.getType() == MemberType.REMOVED) {
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
            List<Constructor<?>> visible = new ArrayList<Constructor<?>>(meth.length);
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
                        } else if (i.getType() == MemberType.REMOVED) {
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
            Constructor<?> meth = clazz.getConstructor(parameters);
            return meth;
        }
        String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
        MethodData md = cd.getMethodData("<init>", args);
        if (md == null) {
            Constructor<?> meth = clazz.getConstructor(parameters);
            return meth;
        }

        switch (md.getType()) {
            case NORMAL:
                Constructor<?> meth = clazz.getConstructor(parameters);
                return meth;
            case FAKE_CONSTRUCTOR:
                try {
                    Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
                    meth = c.getConstructor(parameters);
                    return meth;
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
            Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
            return meth;
        }
        String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
        MethodData md = cd.getMethodData("<init>", args);
        if (md == null) {
            Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
            return meth;
        }

        switch (md.getType()) {
            case NORMAL:
                Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
                return meth;
            case FAKE_CONSTRUCTOR:
                try {
                    Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
                    meth = c.getDeclaredConstructor(parameters);
                    return meth;
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
