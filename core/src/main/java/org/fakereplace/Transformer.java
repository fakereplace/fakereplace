package org.fakereplace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Map;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.detector.DetectorRunner;
import org.fakereplace.manip.Manipulator;
import org.fakereplace.replacement.FakeConstructorUtils;
import org.fakereplace.util.NoInstrument;

import com.google.common.collect.MapMaker;

/**
 * This file is the transformer that instruments classes as they are added to
 * the system.
 * 
 * @author stuart
 * 
 */
public class Transformer implements ClassFileTransformer
{

   Instrumentation instrumentation;

   ClassLoaderInstrumentation classLoaderInstrumenter;

   static Manipulator manipulator = new Manipulator();

   static final String[] replacablePackages;

   static final Map<ClassLoader, Object> integrationClassloader = new MapMaker().weakKeys().makeMap();

   DetectorRunner detector = new DetectorRunner();

   static
   {
      String plist = System.getProperty(Constants.REPLACABLE_PACKAGES_KEY);
      if (plist == null || plist.length() == 0)
      {
         System.out.println("-----------------------------------------------------------------");
         System.out.println("System property " + Constants.REPLACABLE_PACKAGES_KEY + " was not specified, fakereplace is diabled");
         System.out.println("-----------------------------------------------------------------");
         replacablePackages = new String[0];
      }
      else
      {
         replacablePackages = plist.split(",");
      }
   }

   Transformer(Instrumentation i)
   {

      instrumentation = i;
      classLoaderInstrumenter = new ClassLoaderInstrumentation(instrumentation);
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
      // field level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;)[Ljava/lang/annotation/Annotation;", null);
      // method level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[[Ljava/lang/annotation/Annotation;", null);
      // constructor level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Constructor;Ljava/lang/Class;)Z", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[Ljava/lang/annotation/Annotation;", null);
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.AnnotationDelegate", "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Constructor;)[[Ljava/lang/annotation/Annotation;", null);

      // replace method invocation
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null);
      // replace constructor invocation
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Constructor", "org.fakereplace.reflection.ConstructorReflectionDelegate", "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", null);

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

      Thread t = new Thread(detector);
      t.start();

   }

   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
   {
      try
      {
         classLoaderInstrumenter.instrumentClassLoaderIfNessesary(loader, className);
         ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(classfileBuffer)));

         if (classBeingRedefined == null)
         {
            BaseClassData data = new BaseClassData(file, loader);
            ClassDataStore.saveClassData(loader, data.getInternalName(), data);
         }

         // we do not instrument any classes from fakereplace
         // if we did we get an endless loop
         // we also aviod instrumenting much of the java/lang and
         // java/io namespace except for java/lang/reflect/Proxy
         if (BuiltinClassData.skipInstrumentation(className))
         {
            return null;
         }

         if (classBeingRedefined == null)
         {
            AnnotationsAttribute at = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.invisibleTag);
            if (at != null)
            {
               Object an = at.getAnnotation(NoInstrument.class.getName());
               if (an != null)
               {
                  return null;
               }
            }
         }

         // hook in seam support
         // this is such a massive hack
         if (file.getName().equals("org.jboss.seam.servlet.SeamFilter"))
         {
            integrationClassloader.put(loader, new Object());
            // we need to load the class in another thread
            // otherwise it will not be instrumented
            ThreadLoader.loadAsync("org.fakereplace.integration.seam.ClassRedefinitionPlugin", loader, true);
         }

         manipulator.transformClass(file, loader);

         if (!file.isInterface() && isClassReplacable(file.getName()) && (AccessFlag.ENUM & file.getAccessFlags()) == 0)
         {
            detector.addClassLoader(loader, file.getName());
            addMethodForInstrumentation(file);
            addFieldForInstrumentation(file);
            addConstructorForInstrumentation(file);
         }
         ByteArrayOutputStream bs = new ByteArrayOutputStream();
         file.write(new DataOutputStream(bs));

         return bs.toByteArray();
      }
      catch (Throwable e)
      {
         e.printStackTrace();

         throw new IllegalClassFormatException();
      }
   }

   public static boolean isClassReplacable(String className)
   {
      for (String i : replacablePackages)
      {
         if (className.startsWith(i))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Adds a method to a class that re can redefine when the class is reloaded
    * 
    * @param file
    * @throws DuplicateMemberException
    */
   public void addMethodForInstrumentation(ClassFile file) throws DuplicateMemberException
   {
      try
      {
         MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
         m.setAccessFlags(0 | AccessFlag.PUBLIC);

         Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
         b.add(Bytecode.ACONST_NULL);
         b.add(Bytecode.ARETURN);
         CodeAttribute ca = b.toCodeAttribute();
         m.setCodeAttribute(ca);
         file.addMethod(m);
      }
      catch (DuplicateMemberException e)
      {
         // e.printStackTrace();
      }
      try
      {
         MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_STATIC_METHOD_NAME, Constants.ADDED_STATIC_METHOD_DESCRIPTOR);
         m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
         Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
         b.add(Bytecode.ACONST_NULL);
         b.add(Bytecode.ARETURN);
         CodeAttribute ca = b.toCodeAttribute();
         m.setCodeAttribute(ca);
         file.addMethod(m);

      }
      catch (DuplicateMemberException e)
      {
         // e.printStackTrace();
      }
   }

   void addConstructorForInstrumentation(ClassFile file)
   {

      MethodInfo ret = new MethodInfo(file.getConstPool(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
      Bytecode code = new Bytecode(file.getConstPool());
      // if the class does not have a constructor return
      if (!FakeConstructorUtils.addBogusConstructorCall(file, code))
      {
         return;
      }
      CodeAttribute ca = code.toCodeAttribute();
      ca.setMaxLocals(4);
      ret.setCodeAttribute(ca);
      ret.setAccessFlags(AccessFlag.PUBLIC);
      try
      {
         ca.computeMaxStack();
         file.addMethod(ret);
      }
      catch (DuplicateMemberException e)
      {

      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   /**
    * Add's a field to the class that can store data from added fields
    * 
    * @param file
    * @throws DuplicateMemberException
    */
   public void addFieldForInstrumentation(ClassFile file) throws DuplicateMemberException
   {
      try
      {
         if ((file.getAccessFlags() & AccessFlag.INTERFACE) == 0)
         {
            FieldInfo m = new FieldInfo(file.getConstPool(), Constants.ADDED_FIELD_NAME, Constants.ADDED_FIELD_DESCRIPTOR);
            m.setAccessFlags(0 | AccessFlag.PUBLIC);
            file.addField(m);
         }
      }
      catch (DuplicateMemberException e)
      {
         // e.printStackTrace();
      }

   }

   public static Manipulator getManipulator()
   {
      return manipulator;
   }

   public static byte[] getIntegrationClass(ClassLoader c, String name)
   {
      if (!integrationClassloader.containsKey(c))
      {
         return null;
      }
      URL resource = ClassLoader.getSystemClassLoader().getResource(name.replace('.', '/') + ".class");
      InputStream in = null;
      try
      {
         in = resource.openStream();
         return org.fakereplace.util.FileReader.readFileBytes(resource.openStream());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         try
         {
            if (in != null)
            {
               in.close();
            }
         }
         catch (IOException e)
         {
         }
      }
   }
}
