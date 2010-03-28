package org.fakereplace.reflection;

import java.lang.reflect.Constructor;
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

public class ConstructorReflectionDelegate
{

   public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null)
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
            if (i.getType() == MemberType.FAKE)
            {
               Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
               visible.add(i.getConstructor(c));
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
         ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null)
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
               if (i.getType() == MemberType.FAKE && AccessFlag.isPublic(i.getAccessFlags()))
               {
                  Class<?> c = clazz.getClassLoader().loadClass(i.getClassName());
                  visible.add(i.getConstructor(c));
               }
               else if (i.getType() == MemberType.REMOVED_METHOD)
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

   public static Constructor<?> getConstructor(Class<?> clazz, String name, Class<?>... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null)
      {
         Constructor<?> meth = clazz.getConstructor(parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      MethodData md = cd.getMethodData(name, args);
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
      case FAKE:
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

   public static Constructor<?> getDeclaredConstructor(Class<?> clazz, String name, Class<?>... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null)
      {
         Constructor<?> meth = clazz.getDeclaredConstructor(parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      MethodData md = cd.getMethodData(name, args);
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
      case FAKE:
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
