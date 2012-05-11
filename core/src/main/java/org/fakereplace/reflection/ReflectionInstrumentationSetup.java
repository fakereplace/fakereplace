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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fakereplace.manip.Manipulator;

/**
 * Helper utility class for setting up the reflection call replacements.
 */
public class ReflectionInstrumentationSetup {

    private static final String CLASS = Class.class.getName();
    public static final String METHOD = Method.class.getName();
    public static final String CONSTRUCTOR = Constructor.class.getName();
    public static final String FIELD = Field.class.getName();
    public static final String ANNOTATED_ELEMENT = AnnotatedElement.class.getName();

    public static final String ANNOTATION_REFLECTION = AnnotationReflection.class.getName();
    public static final String METHOD_REFLECTION = MethodReflection.class.getName();
    public static final String FIELD_REFLECTION = FieldReflection.class.getName();
    public static final String CONSTRUCTOR_REFLECTION = ConstructorReflection.class.getName();

    public static void setup(Manipulator manipulator) {
        // initilize the reflection manipulation
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, METHOD_REFLECTION, "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, METHOD_REFLECTION, "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, METHOD_REFLECTION, "getMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, METHOD_REFLECTION, "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", null);

        // constructors
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, CONSTRUCTOR_REFLECTION, "getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, CONSTRUCTOR_REFLECTION, "getDeclaredConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, CONSTRUCTOR_REFLECTION, "getConstructors", "()[Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, CONSTRUCTOR_REFLECTION, "getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", null);

        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, METHOD_REFLECTION, "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CONSTRUCTOR, CONSTRUCTOR_REFLECTION, "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Constructor;)Ljava/lang/Class;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(FIELD, FIELD_REFLECTION, "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Field;)Ljava/lang/Class;", null);

        // class level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, ANNOTATION_REFLECTION, "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/Class;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, ANNOTATION_REFLECTION, "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, ANNOTATION_REFLECTION, "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, ANNOTATION_REFLECTION, "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", null);
        // field level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic(FIELD, ANNOTATION_REFLECTION, "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(FIELD, ANNOTATION_REFLECTION, "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(FIELD, ANNOTATION_REFLECTION, "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(FIELD, ANNOTATION_REFLECTION, "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;)[Ljava/lang/annotation/Annotation;", null);
        // method level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, ANNOTATION_REFLECTION, "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, ANNOTATION_REFLECTION, "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, ANNOTATION_REFLECTION, "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, ANNOTATION_REFLECTION, "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, ANNOTATION_REFLECTION, "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[[Ljava/lang/annotation/Annotation;", null);
        // constructor level annotations
        manipulator.replaceVirtualMethodInvokationWithStatic(CONSTRUCTOR, ANNOTATION_REFLECTION, "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Constructor;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CONSTRUCTOR, ANNOTATION_REFLECTION, "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CONSTRUCTOR, ANNOTATION_REFLECTION, "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CONSTRUCTOR, ANNOTATION_REFLECTION, "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CONSTRUCTOR, ANNOTATION_REFLECTION, "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[[Ljava/lang/annotation/Annotation;", null);
        // AnnotatedElement
        manipulator.replaceVirtualMethodInvokationWithStatic(ANNOTATED_ELEMENT, ANNOTATION_REFLECTION, "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/AnnotatedElement;Ljava/lang/Class;)Z", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(ANNOTATED_ELEMENT, ANNOTATION_REFLECTION, "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/AnnotatedElement;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(ANNOTATED_ELEMENT, ANNOTATION_REFLECTION, "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/AnnotatedElement;)[Ljava/lang/annotation/Annotation;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(ANNOTATED_ELEMENT, ANNOTATION_REFLECTION, "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/AnnotatedElement;)[Ljava/lang/annotation/Annotation;", null);

        // method modifiers
        manipulator.replaceVirtualMethodInvokationWithStatic(METHOD, METHOD_REFLECTION, "getModifiers", "()I", "(Ljava/lang/reflect/Method;)I", null);
        // fields
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, FIELD_REFLECTION, "getField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, FIELD_REFLECTION, "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, FIELD_REFLECTION, "getFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", null);
        manipulator.replaceVirtualMethodInvokationWithStatic(CLASS, FIELD_REFLECTION, "getDeclaredFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", null);

    }

    private ReflectionInstrumentationSetup () {

    }
}
