package org.fakereplace.reflection;

import org.fakereplace.manip.Manipulator;

public class ReflectionInstrumentationSetup
{
   public static void setup(Manipulator manipulator)
   {
      // initilize the reflection manipulation
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", null);

      // constructors
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getDeclaredConstructor", "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getConstructors", "()[Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", null);

      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.ConstructorReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Constructor;)Ljava/lang/Class;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Field;)Ljava/lang/Class;", null);

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

      // replace method invocation
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null);
      // replace constructor invocation
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.ConstructorReflectionDelegate", "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", null);
      // method modifiers
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "getModifiers", "()I", "(Ljava/lang/reflect/Method;)I", null);
      // fields
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", null);

      // field access setters
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setBoolean", "(Ljava/lang/Object;Z)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Z)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setByte", "(Ljava/lang/Object;B)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;B)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setChar", "(Ljava/lang/Object;C)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;C)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setDouble", "(Ljava/lang/Object;D)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;D)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setFloat", "(Ljava/lang/Object;F)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;F)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setInt", "(Ljava/lang/Object;I)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;I)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setLong", "(Ljava/lang/Object;J)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;J)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setShort", "(Ljava/lang/Object;S)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;S)V", null);

      // field access getters
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Ljava/lang/Object;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getBoolean", "(Ljava/lang/Object;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getByte", "(Ljava/lang/Object;)B", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)B", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getChar", "(Ljava/lang/Object;)C", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)C", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getDouble", "(Ljava/lang/Object;)D", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)D", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getFloat", "(Ljava/lang/Object;)F", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)F", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getInt", "(Ljava/lang/Object;)I", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)I", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getLong", "(Ljava/lang/Object;)J", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)J", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getShort", "(Ljava/lang/Object;)S", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)S", null);

      // accessible objects
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "setAccessible", "(Z)V", "(Ljava/lang/reflect/AccessibleObject;Z)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "isAccessible", "()Z", "(Ljava/lang/reflect/AccessibleObject;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "setAccessible", "(Z)V", "(Ljava/lang/reflect/AccessibleObject;Z)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "isAccessible", "()Z", "(Ljava/lang/reflect/AccessibleObject;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "setAccessible", "(Z)V", "(Ljava/lang/reflect/AccessibleObject;Z)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "isAccessible", "()Z", "(Ljava/lang/reflect/AccessibleObject;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.AccessibleObject", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "setAccessible", "(Z)V", "(Ljava/lang/reflect/AccessibleObject;Z)V", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.AccessibleObject", "org.fakereplace.reflection.AccessibleObjectReflectionDelegate", "isAccessible", "()Z", "(Ljava/lang/reflect/AccessibleObject;)Z", null);

   }
}
