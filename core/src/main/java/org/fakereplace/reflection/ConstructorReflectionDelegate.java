package org.fakereplace.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;

import org.fakereplace.ConstructorArgument;
import org.fakereplace.boot.Constants;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.util.DescriptorUtils;
import org.fakereplace.util.InvocationUtil;

import sun.reflect.Reflection;

public class ConstructorReflectionDelegate
{

   @SuppressWarnings("restriction")
   public static Object newInstance(Constructor<?> method, Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException
   {
      if (InvocationUtil.executeFakeCall(method))
      {
         MethodData data = ClassDataStore.getMethodInformation(method.getDeclaringClass().getName());
         Class<?> info = ClassDataStore.getRealClassFromProxyName(method.getDeclaringClass().getName());
         try
         {
            Constructor<?> invoke = info.getConstructor(int.class, Object[].class, ConstructorArgument.class);
            Object ar = args;
            if (ar == null)
            {
               ar = new Object[0];
            }
            return invoke.newInstance(data.getMethodNo(), ar, null);
         }
         catch (NoSuchMethodException e)
         {
            throw new RuntimeException(e);
         }
         catch (SecurityException e)
         {
            throw new RuntimeException(e);
         }

      }

      if (!AccessibleObjectReflectionDelegate.isAccessible(method))
      {
         // todo: cache these checks
         Class<?> caller = sun.reflect.Reflection.getCallerClass(2);
         Reflection.ensureMemberAccess(caller, method.getDeclaringClass(), null, method.getModifiers());
      }
      method.setAccessible(true);
      return method.newInstance(args);

   }

   public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null || !cd.isReplaceable())
         {
            return clazz.getDeclaredConstructors();
         }
         Constructor<?>[] meth = clazz.getDeclaredConstructors();
         List<Constructor<?>> visible = new ArrayList<Constructor<?>>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            if (meth[i].getParameterTypes().length != 3 || !meth[i].getParameterTypes()[2].equals(ConstructorArgument.class))
            {
               visible.add(meth[i]);
            }
         }

         for (MethodData i : cd.getMethods())
         {
            if (i.getType() == MemberType.FAKE_CONSTRUCTOR)
            {
               Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
               visible.add(i.getConstructor(c));
            }
            else if (i.getType() == MemberType.REMOVED)
            {
               Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
               visible.remove(i.getConstructor(c));
            }
         }

         Constructor<?>[] ret = new Constructor[visible.size()];
         for (int i = 0; i < visible.size(); ++i)
         {
            ret[i] = visible.get(i);
         }

         return ret;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public static Constructor<?>[] getConstructors(Class<?> clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null || !cd.isReplaceable())
         {
            return clazz.getConstructors();
         }

         Constructor<?>[] meth = clazz.getConstructors();
         List<Constructor<?>> visible = new ArrayList<Constructor<?>>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            if (meth[i].getParameterTypes().length != 3 || !meth[i].getParameterTypes()[2].equals(ConstructorArgument.class))
            {
               visible.add(meth[i]);
            }
         }

         ClassData cta = cd;
         while (cta != null)
         {
            for (MethodData i : cta.getMethods())
            {
               if (i.getType() == MemberType.FAKE_CONSTRUCTOR && AccessFlag.isPublic(i.getAccessFlags()))
               {
                  Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                  visible.add(i.getConstructor(c));
               }
               else if (i.getType() == MemberType.REMOVED)
               {
                  Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                  visible.remove(i.getConstructor(c));
               }
            }
            cta = cta.getSuperClassInformation();
         }

         Constructor<?>[] ret = new Constructor[visible.size()];
         for (int i = 0; i < visible.size(); ++i)
         {
            ret[i] = visible.get(i);
         }
         return ret;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null || !cd.isReplaceable())
      {
         Constructor<?> meth = clazz.getConstructor(parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      MethodData md = cd.getMethodData("<init>", args);
      if (md == null)
      {
         Constructor<?> meth = clazz.getConstructor(parameters);
         return meth;
      }

      switch (md.getType())
      {
      case NORMAL:
         Constructor<?> meth = clazz.getConstructor(parameters);
         return meth;
      case FAKE_CONSTRUCTOR:
         try
         {
            Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
            meth = c.getConstructor(parameters);
            return meth;
         }
         catch (NoSuchMethodException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      throw new NoSuchMethodException();
   }

   public static Constructor<?> getDeclaredConstructor(Class<?> clazz, Class<?>... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null || !cd.isReplaceable())
      {
         Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      MethodData md = cd.getMethodData("<init>", args);
      if (md == null)
      {
         Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
         return meth;
      }

      switch (md.getType())
      {
      case NORMAL:
         Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
         return meth;
      case FAKE_CONSTRUCTOR:
         try
         {
            Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
            meth = c.getDeclaredConstructor(parameters);
            return meth;
         }
         catch (NoSuchMethodException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      throw new NoSuchMethodException();
   }

   public static Class<?> getDeclaringClass(Constructor<?> f)
   {
      Class<?> c = f.getDeclaringClass();
      if (c.getName().startsWith(Constants.GENERATED_CLASS_PACKAGE))
      {
         return ClassDataStore.getRealClassFromProxyName(c.getName());
      }
      return c;
   }
}
