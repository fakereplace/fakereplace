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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fakereplace.core.Constants;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.MemberType;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;
import sun.reflect.Reflection;

/**
 * Class that handles access to re-written fields.
 *
 * @author stuart
 */
public class FieldReflection {

    public static Class<?> getDeclaringClass(Field f) {
        Class<?> c = f.getDeclaringClass();
        if (c.getName().startsWith(Constants.GENERATED_CLASS_PACKAGE)) {
            return ClassDataStore.instance().getRealClassFromProxyName(c.getName());
        }
        return c;
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        if (!ClassDataStore.instance().isClassReplaced(clazz)) {
            return clazz.getDeclaredFields();
        }
        try {
            ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));
            Field[] meth = clazz.getDeclaredFields();

            Collection<FieldData> fieldData = cd.getFields();
            List<Field> visible = new ArrayList<>(meth.length);
            for (int i = 0; i < meth.length; ++i) {
                for (FieldData f : fieldData) {
                    if (f.getAccessFlags() == meth[i].getModifiers() && f.getName().equals(meth[i].getName())) {
                        if (f.getMemberType() == MemberType.NORMAL) {
                            visible.add(meth[i]);
                            break;
                        }
                    }
                }
            }

            for (FieldData i : cd.getFields()) {
                if (i.getMemberType() == MemberType.FAKE) {
                    Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                    visible.add(i.getField(c));
                }
            }

            Field[] ret = new Field[visible.size()];
            for (int i = 0; i < visible.size(); ++i) {
                ret[i] = visible.get(i);
            }

            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field[] getFields(Class<?> clazz) {
        if (!ClassDataStore.instance().isClassReplaced(clazz)) {
            return clazz.getFields();
        }
        try {
            ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

            if (cd == null) {
                return clazz.getDeclaredFields();
            }

            Field[] meth = clazz.getFields();
            Collection<FieldData> fieldData = cd.getFields();
            List<Field> visible = new ArrayList<>(meth.length);
            for (int i = 0; i < meth.length; ++i) {
                for (FieldData f : fieldData) {
                    if (f.getAccessFlags() == meth[i].getModifiers() && f.getName().equals(meth[i].getName())) {
                        if (f.getMemberType() == MemberType.NORMAL) {
                            visible.add(meth[i]);
                            break;
                        }
                    }
                }
            }

            ClassData cta = cd;
            while (cta != null) {
                for (FieldData i : cta.getFields()) {
                    if (i.getMemberType() == MemberType.FAKE && AccessFlag.isPublic(i.getAccessFlags())) {
                        Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                        visible.add(i.getField(c));
                    }
                }
                cta = cta.getSuperClassInformation();
            }

            Field[] ret = new Field[visible.size()];
            for (int i = 0; i < visible.size(); ++i) {
                ret[i] = visible.get(i);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (!ClassDataStore.instance().isClassReplaced(clazz)) {
            return clazz.getField(name);
        }
        ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

        if (cd == null) {
            return clazz.getField(name);
        }
        FieldData fd = cd.getField(name);
        if (fd == null) {
            return clazz.getField(name);
        }
        if (!AccessFlag.isPublic(fd.getAccessFlags())) {
            throw new NoSuchFieldException(clazz.getName() + "." + name);
        }
        switch (fd.getMemberType()) {

            case NORMAL:
                return clazz.getField(name);
            case FAKE:
                try {
                    Class<?> c = clazz.getClassLoader().loadClass(fd.getClassName());
                    return c.getField(name);

                } catch (NoSuchFieldException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
        throw new NoSuchFieldException();
    }

    public static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (!ClassDataStore.instance().isClassReplaced(clazz)) {
            return clazz.getDeclaredField(name);
        }

        ClassData cd = ClassDataStore.instance().getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

        if (cd == null) {
            return clazz.getDeclaredField(name);
        }
        FieldData fd = cd.getField(name);
        if (fd == null) {
            return clazz.getDeclaredField(name);
        }

        switch (fd.getMemberType()) {
            case NORMAL:
                return clazz.getDeclaredField(name);
            case FAKE:
                try {
                    Class<?> c = clazz.getClassLoader().loadClass(fd.getClassName());
                    return c.getDeclaredField(name);
                } catch (NoSuchFieldException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }
        throw new NoSuchFieldException();
    }

    public static void set(Field f, Object object, Object val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setBoolean(Field f, Object object, boolean val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setByte(Field f, Object object, byte val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setChar(Field f, Object object, char val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setDouble(Field f, Object object, double val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setFloat(Field f, Object object, float val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setInt(Field f, Object object, int val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setLong(Field f, Object object, long val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static void setShort(Field f, Object object, short val) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        accessor.set(object, val);
    }

    public static Object get(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return accessor.get(object);
    }

    public static boolean getBoolean(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Boolean) accessor.get(object);
    }

    public static byte getByte(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Byte) accessor.get(object);

    }

    public static char getChar(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Character) accessor.get(object);

    }

    public static Double getDouble(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Double) accessor.get(object);
    }

    public static float getFloat(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Float) accessor.get(object);
    }

    public static int getInt(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Integer) accessor.get(object);
    }

    public static long getLong(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Long) accessor.get(object);
    }

    public static Object getShort(Field f, Object object) throws IllegalAccessException {
        FieldAccessor accessor = ClassDataStore.instance().getFieldAccessor(f.getDeclaringClass().getName());
        if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible()) {
            Class<?> caller = findCallerClass();
            Reflection.ensureMemberAccess(caller, accessor.getDeclaringClass(), null, f.getModifiers());
        }
        return (Short) accessor.get(object);
    }


    private static Class findCallerClass() {
        Class<?> c =  Reflection.getCallerClass(3);
        if(c == FieldReflection.class) {
            return Reflection.getCallerClass(4);
        }
        return c;
    }

    public static boolean isFakeField(Field f) {
        if (f.getDeclaringClass().getName().startsWith(org.fakereplace.core.Constants.GENERATED_CLASS_PACKAGE)) {
            return true;
        }
        return false;
    }

}
