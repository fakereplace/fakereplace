package org.fakereplace.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.data.ModifiedMethod;
import org.fakereplace.util.DescriptorUtils;
import org.fakereplace.util.InvocationUtil;

import sun.reflect.Reflection;

/**
 * This class has some method related reflection calls delegated to it at
 * runtime by bytecode manipulation
 * 
 * @author stuart
 * 
 */
public class MethodReflection
{
   public static int getModifiers(Method method)
   {
      if (method.isAnnotationPresent(ModifiedMethod.class))
      {
         return method.getModifiers() | Modifier.FINAL;
      }
      return method.getModifiers();
   }

   public static Object invoke(Method method, Object instance, Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      if (!Modifier.isStatic(method.getModifiers()))
      {
         MethodData info = ClassDataStore.getMethodInformation(method.getDeclaringClass().getName());
         try
         {
            Method invoke = info.getMethodToInvoke(method.getDeclaringClass());
            Object[] newAgrs = InvocationUtil.prepare(instance, args);
            return invoke.invoke(null, newAgrs);
         }
         catch (NoSuchMethodException e)
         {
            throw new RuntimeException(e);
         }
         catch (SecurityException e)
         {
            throw new RuntimeException(e);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException(e);
         }
      }

      if (!AccessibleObjectReflectionDelegate.isAccessible(method))
      {
         // todo: cache these checks
         Class<?> caller = sun.reflect.Reflection.getCallerClass(2);
         Reflection.ensureMemberAccess(caller, method.getDeclaringClass(), instance, method.getModifiers());
      }
      method.setAccessible(true);
      return method.invoke(instance, args);
   }

   public static Method[] getDeclaredMethods(Class<?> clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null || !cd.isReplaceable())
         {
            return clazz.getDeclaredMethods();
         }
         Method[] meth = clazz.getDeclaredMethods();
         List<Method> visible = new ArrayList<Method>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            MethodData mData = cd.getData(meth[i]);
            if (mData == null || mData.getType() == MemberType.NORMAL)
            {
               visible.add(meth[i]);
            }
         }

         for (MethodData i : cd.getMethods())
         {
            if (i.getType() == MemberType.FAKE)
            {
               Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
               visible.add(i.getMethod(c));
            }
         }

         Method[] ret = new Method[visible.size()];
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

   public static Method[] getMethods(Class<?> clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null)
         {
            return clazz.getMethods();
         }

         Method[] meth = clazz.getMethods();
         List<Method> visible = new ArrayList<Method>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            MethodData mData = cd.getData(meth[i]);
            if (mData == null || mData.getType() == MemberType.NORMAL)
            {
               visible.add(meth[i]);
            }
         }

         ClassData cta = cd;
         while (cta != null)
         {
            if (cta.isReplaceable())
            {
               for (MethodData i : cta.getMethods())
               {
                  if (i.getType() == MemberType.FAKE && AccessFlag.isPublic(i.getAccessFlags()))
                  {
                     Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                     visible.add(i.getMethod(c));
                  }
                  else if (i.getType() == MemberType.REMOVED)
                  {
                     Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                     visible.remove(i.getMethod(c));
                  }
               }
            }
            cta = cta.getSuperClassInformation();
         }

         Method[] ret = new Method[visible.size()];
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

   public static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null)
      {
         Method meth = clazz.getMethod(name, parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      MethodData md = cd.getMethodData(name, args);
      Class<?> superClass = clazz;
      while (superClass.getSuperclass() != null && md == null && superClass != Object.class)
      {
         superClass = superClass.getSuperclass();
         cd = ClassDataStore.getModifiedClassData(superClass.getClassLoader(), Descriptor.toJvmName(superClass.getName()));
         if (cd != null)
         {
            md = cd.getMethodData(name, args);
         }
      }

      if (md == null)
      {
         Method meth = clazz.getMethod(name, parameters);
         return meth;
      }

      switch (md.getType())
      {
      case NORMAL:
         Method meth = superClass.getMethod(name, parameters);
         return meth;
      case FAKE:
         try
         {
            if (!AccessFlag.isPublic(md.getAccessFlags()))
            {
               throw new NoSuchMethodException(clazz.getName() + "." + name);
            }
            Class<?> c = superClass.getClassLoader().loadClass(md.getClassName());
            meth = c.getMethod(name, parameters);
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

   public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameters) throws NoSuchMethodException
   {
      ClassData cd = ClassDataStore.getModifiedClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));
      if (cd == null || !cd.isReplaceable())
      {
         Method meth = clazz.getDeclaredMethod(name, parameters);
         return meth;
      }
      String args;
      if (parameters != null)
      {
         args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      }
      else
      {
         args = "()";
      }
      MethodData md = cd.getMethodData(name, args);
      if (md == null)
      {
         Method meth = clazz.getDeclaredMethod(name, parameters);
         return meth;
      }

      switch (md.getType())
      {
      case NORMAL:
         Method meth = clazz.getDeclaredMethod(name, parameters);
         return meth;
      case FAKE:
         try
         {
            Class<?> c = clazz.getClassLoader().loadClass(md.getClassName());
            meth = c.getDeclaredMethod(name, parameters);

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

   public static Class<?> getDeclaringClass(Method m)
   {
      Class<?> c = m.getDeclaringClass();
      if (c.getName().startsWith(Constants.GENERATED_CLASS_PACKAGE))
      {
         return ClassDataStore.getRealClassFromProxyName(c.getName());
      }
      return c;
   }

   public static boolean fakeCallRequired(Method method)
   {
      return method.getDeclaringClass().getName().startsWith(Constants.GENERATED_CLASS_PACKAGE);
   }
}
