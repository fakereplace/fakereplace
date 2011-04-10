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

package org.fakereplace.reflection;

import org.fakereplace.manip.Manipulator;

public class ReflectionInstrumentationSetup {
    public static void setup(Manipulator manipulator) {
        // initilize the reflection manipulation
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.MethodReflection", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.MethodReflection", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.MethodReflection", "getMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.MethodReflection", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", null);

        // constructors
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getDeclaredConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getConstructors", "()[Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", null);

        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.MethodReflection", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Constructor;)Ljava/lang/Class;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldReflection", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Field;)Ljava/lang/Class;", null);

        // class level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/Class;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", null);
        // field level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;)[Ljava/lang/annotation/Annotation;", null);
        // method level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[[Ljava/lang/annotation/Annotation;", null);
        // constructor level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Constructor;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[[Ljava/lang/annotation/Annotation;", null);
        // AnnotatedElement
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.AnnotatedElement", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/AnnotatedElement;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.AnnotatedElement", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/AnnotatedElement;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.AnnotatedElement", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/AnnotatedElement;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.AnnotatedElement", "org.fakereplace.reflection.AnnotationDelegate", "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/AnnotatedElement;)[Ljava/lang/annotation/Annotation;", null);

        // method modifiers
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.MethodReflection", "getModifiers", "()I", "(Ljava/lang/reflect/Method;)I", null);
        // fields
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.FieldReflection", "getField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.FieldReflection", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.FieldReflection", "getFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.FieldReflection", "getDeclaredFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", null);

    }
}
