package org.fakereplace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.concurrent.locks.ReentrantLock;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.manip.Manipulator;
import org.fakereplace.util.InvocationUtil;
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

   static final String[] replacablePackages;

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
      manipulator.replaceVirtualMethodInvokationWithStatic(

      "java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;");

      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Method;)Ljava/lang/Class;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaringClass", "()Ljava/lang/Class;", "(Ljava/lang/reflect/Field;)Ljava/lang/Class;");

      // class level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/Class;Ljava/lang/Class;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;");
      // field level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Field;)[Ljava/lang/annotation/Annotation;");
      // method level annotations
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "isAnnotationPresent", "(Ljava/lang/Class;)Z", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotation", "(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getAnnotations", "()[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[Ljava/lang/annotation/Annotation;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.AnnotationDelegate", "getParameterAnnotations", "()[[Ljava/lang/annotation/Annotation;", "(Ljava/lang/reflect/Method;)[[Ljava/lang/annotation/Annotation;");

      // replace method invocation
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
      // getModifiers
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Method", "org.fakereplace.reflection.ReflectionDelegate", "getModifiers", "()I", "(Ljava/lang/reflect/Method;)I");

      // fields
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.Class", "org.fakereplace.reflection.ReflectionDelegate", "getDeclaredFields", "()[Ljava/lang/reflect/Field;", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;");

      // field access setters
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setBoolean", "(Ljava/lang/Object;Z)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Z)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setByte", "(Ljava/lang/Object;B)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;B)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setChar", "(Ljava/lang/Object;C)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;C)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setDouble", "(Ljava/lang/Object;D)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;D)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setFloat", "(Ljava/lang/Object;F)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;F)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setInt", "(Ljava/lang/Object;I)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;I)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setLong", "(Ljava/lang/Object;J)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;J)V");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "setShort", "(Ljava/lang/Object;S)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;S)V");

      // field access getters
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Ljava/lang/Object;");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getBoolean", "(Ljava/lang/Object;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Z");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getByte", "(Ljava/lang/Object;)B", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)B");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getChar", "(Ljava/lang/Object;)C", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)C");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getDouble", "(Ljava/lang/Object;)D", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)D");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getFloat", "(Ljava/lang/Object;)F", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)F");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getInt", "(Ljava/lang/Object;)I", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)I");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getLong", "(Ljava/lang/Object;)J", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)J");
      manipulator.replaceVirtualMethodInvokationWithStatic("java.lang.reflect.Field", "org.fakereplace.reflection.FieldAccess", "getShort", "(Ljava/lang/Object;)S", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)S");

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

         if (!file.isInterface() && isClassReplacable(file.getName()))
         {
            addMethodForInstrumentation(file);
            addFieldForInstrumentation(file);
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

   /**
    * Adds a method to a class that re can redefine when the class is reloaded
    * 
    * @param file
    * @throws DuplicateMemberException
    * @throws  
    */
   public void addMethodCallingMethod(ClassFile file) throws DuplicateMemberException, BadBytecode
   {
      try
      {
         MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_CALLING_METHOD, Constants.ADDED_METHOD_CALLING_METHOD_DESCRIPTOR);
         m.setAccessFlags(0 | AccessFlag.PUBLIC | AccessFlag.STATIC);

         // we test if the method is static and the first argment is not null.
         // if this is the case we see if it is a fakereplace method
         // if it is then we call it

         // we build up the conditionals from the inside out. The first section is the code to make a fakereplace call
         Bytecode fakeCall = new Bytecode(file.getConstPool());
         // first the first two args
         fakeCall.addAload(0);
         fakeCall.add(Opcode.ACONST_NULL);

         fakeCall.add(Opcode.ALOAD_1);
         fakeCall.add(Opcode.ALOAD_2);
         fakeCall.addInvokestatic(InvocationUtil.class.getName(), "prepare", "(Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;");
         // now we have an array with all the args on top of the stack
         // invoke the method
         fakeCall.addInvokevirtual(Method.class.getName(), "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
         fakeCall.addOpcode(Opcode.ARETURN);
         // now fakecall is complete. we only want to execute this bytecode if the object is not null and the method is
         // static
         // we delegate this check to another function because it is easier
         Bytecode conditional = new Bytecode(file.getConstPool());
         conditional.addAload(0);
         conditional.addAload(1);
         conditional.addInvokestatic(InvocationUtil.class.getName(), "executeFakeCall", "(Ljava/lang/reflect/Method;Ljava/lang/Object;)Z");
         conditional.add(Opcode.ICONST_0);
         conditional.add(Opcode.IF_ICMPEQ);
         conditional.addIndex(fakeCall.length() + 3);

         Bytecode b = new Bytecode(file.getConstPool());
         b.add(Opcode.ALOAD_0);
         b.add(Opcode.ALOAD_1);
         b.add(Opcode.ALOAD_2);
         b.addInvokevirtual(Method.class.getName(), "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
         b.addOpcode(Opcode.ARETURN);
         CodeAttribute ca = b.toCodeAttribute();
         ca.iterator().insert(fakeCall.get());
         ca.iterator().insert(conditional.get());
         m.setCodeAttribute(ca);
         ca.setMaxLocals(3);
         ca.computeMaxStack();
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
         MemberType type = MemberType.NORMAL;
         if ((m.getDescriptor().equals(Constants.ADDED_METHOD_DESCRIPTOR) && m.getName().equals(Constants.ADDED_METHOD_NAME)) || (m.getDescriptor().equals(Constants.ADDED_STATIC_METHOD_DESCRIPTOR) && m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME)))
         {
            type = MemberType.ADDED_SYSTEM;
         }

         MethodData md = new MethodData(m.getName(), m.getDescriptor(), file.getName(), type, m.getAccessFlags());
         data.addMethod(md);
      }
      for (Object o : file.getFields())
      {
         FieldInfo m = (FieldInfo) o;
         if (m.getName().equals(Constants.ADDED_FIELD_NAME))
         {
            data.addField(m, MemberType.ADDED_SYSTEM, file.getName());
         }
         else
         {
            data.addField(m, MemberType.NORMAL, file.getName());
         }
      }

      ClassDataStore.saveClassData(loader, data.getInternalName(), data);
   }

   public static Manipulator getManipulator()
   {
      return manipulator;
   }

}
