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

package org.fakereplace.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.ParameterAnnotationsAttribute;
import org.fakereplace.classloading.ProxyDefinitionStore;

/**
 * Stores information about the annotations on reloaded classes
 * <p/>
 * TODO: This leaks Class's. everthing should be stored in weak hashaps keyed on
 * the class object
 *
 * @author stuart
 */
public class AnnotationDataStore {

    private static Map<Class<?>, Annotation[]> classAnnotations = new ConcurrentHashMap<Class<?>, Annotation[]>();

    private static Map<Class<?>, Map<Class<? extends Annotation>, Annotation>> classAnnotationsByType = new ConcurrentHashMap<Class<?>, Map<Class<? extends Annotation>, Annotation>>();

    private static Map<Field, Annotation[]> fieldAnnotations = new ConcurrentHashMap<Field, Annotation[]>();

    private static Map<Field, Map<Class<? extends Annotation>, Annotation>> fieldAnnotationsByType = new ConcurrentHashMap<Field, Map<Class<? extends Annotation>, Annotation>>();

    private static Map<Method, Annotation[]> methodAnnotations = new ConcurrentHashMap<Method, Annotation[]>();

    private static Map<Method, Map<Class<? extends Annotation>, Annotation>> methodAnnotationsByType = new ConcurrentHashMap<Method, Map<Class<? extends Annotation>, Annotation>>();

    private static Map<Method, Annotation[][]> parameterAnnotations = new ConcurrentHashMap<Method, Annotation[][]>();

    private static Map<Constructor<?>, Annotation[]> constructorAnnotations = new ConcurrentHashMap<Constructor<?>, Annotation[]>();

    private static Map<Constructor<?>, Map<Class<? extends Annotation>, Annotation>> constructorAnnotationsByType = new ConcurrentHashMap<Constructor<?>, Map<Class<? extends Annotation>, Annotation>>();

    private static Map<Constructor<?>, Annotation[][]> constructorParameterAnnotations = new ConcurrentHashMap<Constructor<?>, Annotation[][]>();

    static final String PROXY_METHOD_NAME = "annotationsMethod";

    public static boolean isClassDataRecorded(Class<?> clazz) {
        return classAnnotations.containsKey(clazz);
    }

    public static Annotation[] getClassAnnotations(Class<?> clazz) {
        return classAnnotations.get(clazz);
    }

    public static Annotation getClassAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return classAnnotationsByType.get(clazz).get(annotation);
    }

    public static boolean isClassAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotation) {
        return classAnnotationsByType.get(clazz).containsKey(annotation);
    }

    public static boolean isFieldDataRecorded(Field clazz) {
        return fieldAnnotations.containsKey(clazz);
    }

    public static Annotation[] getFieldAnnotations(Field clazz) {
        return fieldAnnotations.get(clazz);
    }

    public static Annotation getFieldAnnotation(Field clazz, Class<? extends Annotation> annotation) {
        return fieldAnnotationsByType.get(clazz).get(annotation);
    }

    public static boolean isFieldAnnotationPresent(Field clazz, Class<? extends Annotation> annotation) {
        return fieldAnnotationsByType.get(clazz).containsKey(annotation);
    }

    public static boolean isMethodDataRecorded(Method clazz) {
        return methodAnnotations.containsKey(clazz);
    }

    public static Annotation[] getMethodAnnotations(Method clazz) {
        return methodAnnotations.get(clazz);
    }

    public static Annotation getMethodAnnotation(Method clazz, Class<? extends Annotation> annotation) {
        return methodAnnotationsByType.get(clazz).get(annotation);
    }

    public static boolean isMethodAnnotationPresent(Method clazz, Class<? extends Annotation> annotation) {
        return methodAnnotationsByType.get(clazz).containsKey(annotation);
    }

    public static Annotation[][] getMethodParameterAnnotations(Method clazz) {
        return parameterAnnotations.get(clazz);
    }

    // constructor

    public static boolean isConstructorDataRecorded(Constructor<?> clazz) {
        return constructorAnnotations.containsKey(clazz);
    }

    public static Annotation[] getConstructorAnnotations(Constructor<?> clazz) {
        return constructorAnnotations.get(clazz);
    }

    public static Annotation getConstructorAnnotation(Constructor<?> clazz, Class<? extends Annotation> annotation) {
        return constructorAnnotationsByType.get(clazz).get(annotation);
    }

    public static boolean isConstructorAnnotationPresent(Constructor<?> clazz, Class<? extends Annotation> annotation) {
        return constructorAnnotationsByType.get(clazz).containsKey(annotation);
    }

    public static Annotation[][] getMethodParameterAnnotations(Constructor<?> clazz) {
        return constructorParameterAnnotations.get(clazz);
    }

    static Class<?> createAnnotationsProxy(ClassLoader loader, AnnotationsAttribute annotations) {
        String proxyName = ProxyDefinitionStore.getProxyName();
        ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
        proxy.setAccessFlags(AccessFlag.PUBLIC);
        AttributeInfo a = annotations.copy(proxy.getConstPool(), Collections.EMPTY_MAP);
        proxy.addAttribute(a);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bytes);
            try {
                proxy.write(dos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ProxyDefinitionStore.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
            return loader.loadClass(proxyName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static Class<?> createParameterAnnotationsProxy(ClassLoader loader, ParameterAnnotationsAttribute annotations, int paramCount) {
        String proxyName = ProxyDefinitionStore.getProxyName();
        ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
        proxy.setAccessFlags(AccessFlag.PUBLIC);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paramCount; ++i) {
            sb.append("I");
        }

        MethodInfo method = new MethodInfo(proxy.getConstPool(), PROXY_METHOD_NAME, "(" + sb.toString() + ")V");
        Bytecode b = new Bytecode(proxy.getConstPool());
        b.add(Opcode.RETURN);
        method.setAccessFlags(AccessFlag.PUBLIC);
        method.setCodeAttribute(b.toCodeAttribute());
        method.getCodeAttribute().setMaxLocals(paramCount + 1);
        AttributeInfo an = annotations.copy(proxy.getConstPool(), Collections.EMPTY_MAP);
        method.addAttribute(an);

        try {
            proxy.addMethod(method);
            method.getCodeAttribute().computeMaxStack();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bytes);
            try {
                proxy.write(dos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ProxyDefinitionStore.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
            return loader.loadClass(proxyName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void recordClassAnnotations(Class<?> clazz, AnnotationsAttribute annotations) {
        // no annotations
        if (annotations == null) {
            Annotation[] ans = new Annotation[0];
            classAnnotations.put(clazz, ans);
            classAnnotationsByType.put(clazz, Collections.EMPTY_MAP);
            return;
        }
        Class<?> pclass = createAnnotationsProxy(clazz.getClassLoader(), annotations);
        classAnnotations.put(clazz, pclass.getAnnotations());
        Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
        classAnnotationsByType.put(clazz, anVals);
        int count = 0;
        for (Annotation a : pclass.getAnnotations()) {
            anVals.put(a.annotationType(), a);
            count++;
        }
    }

    public static void recordFieldAnnotations(Field field, AnnotationsAttribute annotations) {
        // no annotations
        if (annotations == null) {
            Annotation[] ans = new Annotation[0];
            fieldAnnotations.put(field, ans);
            fieldAnnotationsByType.put(field, Collections.EMPTY_MAP);
            return;
        }
        Class<?> pclass = createAnnotationsProxy(field.getDeclaringClass().getClassLoader(), annotations);
        fieldAnnotations.put(field, pclass.getAnnotations());
        Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
        fieldAnnotationsByType.put(field, anVals);
        int count = 0;
        for (Annotation a : pclass.getAnnotations()) {
            anVals.put(a.annotationType(), a);
            count++;
        }
    }

    public static void recordMethodAnnotations(Method method, AnnotationsAttribute annotations) {
        // no annotations
        if (annotations == null) {
            Annotation[] ans = new Annotation[0];
            methodAnnotations.put(method, ans);
            methodAnnotationsByType.put(method, Collections.EMPTY_MAP);
            return;
        }
        Class<?> pclass = createAnnotationsProxy(method.getDeclaringClass().getClassLoader(), annotations);
        methodAnnotations.put(method, pclass.getAnnotations());
        Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
        methodAnnotationsByType.put(method, anVals);
        int count = 0;
        for (Annotation a : pclass.getAnnotations()) {
            anVals.put(a.annotationType(), a);
            count++;
        }
    }

    public static void recordMethodParameterAnnotations(Method method, ParameterAnnotationsAttribute annotations) {
        // no annotations
        if (annotations == null) {
            Annotation[][] ans = new Annotation[method.getParameterAnnotations().length][0];
            parameterAnnotations.put(method, ans);
            return;
        }

        Class<?> pclass = createParameterAnnotationsProxy(method.getDeclaringClass().getClassLoader(), annotations, method.getParameterTypes().length);
        Class<?>[] types = new Class[method.getParameterTypes().length];
        for (int i = 0; i < types.length; ++i) {
            types[i] = int.class;
        }
        try {
            Method anMethod = pclass.getMethod(PROXY_METHOD_NAME, types);
            parameterAnnotations.put(method, anMethod.getParameterAnnotations());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void recordConstructorAnnotations(Constructor<?> constructor, AnnotationsAttribute annotations) {
        // no annotations
        if (annotations == null) {
            Annotation[] ans = new Annotation[0];
            constructorAnnotations.put(constructor, ans);
            constructorAnnotationsByType.put(constructor, Collections.EMPTY_MAP);
            return;
        }
        Class<?> pclass = createAnnotationsProxy(constructor.getDeclaringClass().getClassLoader(), annotations);
        constructorAnnotations.put(constructor, pclass.getAnnotations());
        Map<Class<? extends Annotation>, Annotation> anVals = new HashMap<Class<? extends Annotation>, Annotation>();
        constructorAnnotationsByType.put(constructor, anVals);
        int count = 0;
        for (Annotation a : pclass.getAnnotations()) {
            anVals.put(a.annotationType(), a);
            count++;
        }

    }

    public static void recordConstructorParameterAnnotations(Constructor<?> method, ParameterAnnotationsAttribute annotations) {
        // no annotations
        if (annotations == null) {
            Annotation[][] ans = new Annotation[method.getParameterAnnotations().length][0];
            constructorParameterAnnotations.put(method, ans);
            return;
        }

        Class<?> pclass = createParameterAnnotationsProxy(method.getDeclaringClass().getClassLoader(), annotations, method.getParameterTypes().length);
        Class<?>[] types = new Class[method.getParameterTypes().length];
        for (int i = 0; i < types.length; ++i) {
            types[i] = int.class;
        }
        try {
            Method anMethod = pclass.getMethod(PROXY_METHOD_NAME, types);
            constructorParameterAnnotations.put(method, anMethod.getParameterAnnotations());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
