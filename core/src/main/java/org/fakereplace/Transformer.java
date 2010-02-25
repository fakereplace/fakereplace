package org.fakereplace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.locks.ReentrantLock;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.manip.Manipulator;
import org.fakereplace.util.NoInstrument;

/**
 * This file is the transformer that instruments classes as they are added to
 * the system.
 * 
 * @author stuart
 * 
 */
public class Transformer implements ClassFileTransformer
{

   // we only want one thread to be transforming classes at a time.
   // otherwise lots of problems can result.
   ReentrantLock lock = new ReentrantLock();

   Instrumentation instrumentation;

   ClassLoaderInstrumentation classLoaderInstrumenter;

   static Manipulator manipulator = new Manipulator();

   Transformer(Instrumentation i)
   {
      instrumentation = i;
      classLoaderInstrumenter = new ClassLoaderInstrumentation(instrumentation);
      // initilize the reflection manipulation
      manipulator.replaceVirtualMethodInvokationWithStatic(

      "java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;");

      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;");

      // class level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/Class;Ljava/lang/Class;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;");
      // field level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/Field;Ljava/lang/Class;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/Field;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Field;)[Ljava/lang/annotation/Annotation;");
      // method level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[[Ljava/lang/annotation/Annotation;");

      // fields
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;");
   }

   /**
    * This is set to true once we retransform the first class This is a
    * performance improvement that allows us to skip a large amount of code
    * until we know it is needed
    */
   boolean transformationStarted = false;

   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
   {
      try
      {
         lock.lock();

         if (classBeingRedefined != null)
         {
            transformationStarted = true;
         }
         classLoaderInstrumenter.instrumentClassLoaderIfNessesary(loader, className);
         // we do not instrument any classes from fakereplace
         // if we did we get an endless loop
         // we also aviod instrumenting much of the java/lang and
         // java/io namespace except for java/lang/reflect/Proxy
         if (BuiltinClassData.skipInstrumentation(className))
         {
            return null;
         }

         ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(classfileBuffer)));
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
         replaceReflectionCalls(file);

         if (classBeingRedefined == null)
         {
            recordClassDetails(file, loader);
         }

         if (!file.isInterface())
         {
            addMethodForInstrumentation(file);
            // addFieldForInstrumentation(file);
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
      finally
      {
         lock.unlock();
      }
   }

   /**
    * Replace all calls to java reflection api's with calls to our api's to give
    * us a change to mangle the calls if nessesary
    * 
    * @param file
    */
   void replaceReflectionCalls(ClassFile file)
   {
      manipulator.transformClass(file);
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
         MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
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
         MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_STATIC_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
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
            FieldInfo m = new FieldInfo(file.getConstPool(), Constants.ADDED_FIELD_NAME, "[Ljava/lang/Object;");
            m.setAccessFlags(0 | AccessFlag.PUBLIC);
            Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
            b.add(Bytecode.ACONST_NULL);
            b.add(Bytecode.ARETURN);
            file.addField(m);
         }
      }
      catch (DuplicateMemberException e)
      {
         // e.printStackTrace();
      }

   }

   private void recordClassDetails(ClassFile file, ClassLoader loader)
   {
      ClassData data = new ClassData();
      data.setClassName(file.getName());
      data.setInternalName(Descriptor.toJvmName(file.getName()));
      data.setLoader(loader);
      data.setSuperClassName(Descriptor.toJvmName(file.getSuperclass()));

      for (Object o : file.getMethods())
      {
         MethodInfo m = (MethodInfo) o;
         MethodData md = new MethodData(m.getName(), m.getDescriptor());

         md.setClassName(file.getName());
         md.setAccessFlags(m.getAccessFlags());
         if (md.getDescriptor().equals("(I[Ljava/lang/Object;)Ljava/lang/Object;") && md.getMethodName().equals(Constants.ADDED_METHOD_NAME))
         {
            md.setType(MemberType.ADDED_SYSTEM);
         }
         else
         {
            md.setType(MemberType.NORMAL);
         }
         data.addMethod(md);
      }
      for (Object o : file.getFields())
      {
         FieldInfo m = (FieldInfo) o;
         if (m.getName().equals(Constants.ADDED_FIELD_NAME))
         {
            data.addField(m, MemberType.ADDED_SYSTEM);
         }
         else
         {
            data.addField(m, MemberType.NORMAL);
         }
      }

      ClassDataStore.saveClassData(loader, data.getInternalName(), data);
   }

   public static Manipulator getManipulator()
   {
      return manipulator;
   }

}
