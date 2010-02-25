package org.fakereplace.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.Descriptor;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.util.DescriptorUtils;

/**
 * This class contains static methods. These methods are called instead of
 * Class.get*** due to bytecode instrumentation
 * 
 * @author stuart
 * 
 */
public class ReflectionDelegate
{

   public static Method[] getDeclaredMethods(Class clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null)
         {
            return clazz.getDeclaredMethods();
         }
         Method[] meth = clazz.getDeclaredMethods();
         List<Method> visible = new ArrayList<Method>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            if (!meth[i].getName().equals(Constants.ADDED_METHOD_NAME) && !meth[i].getName().equals(Constants.ADDED_STATIC_METHOD_NAME))
               visible.add(meth[i]);
         }

         for (MethodData i : cd.getMethods())
         {
            if (i.getType() == MemberType.FAKE)
            {
               Class c = clazz.getClassLoader().loadClass(i.getClassName());
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

   public static Method[] getMethods(Class clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null)
         {
            return clazz.getMethods();
         }

         Method[] meth = clazz.getMethods();
         List<Method> visible = new ArrayList<Method>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            if (!meth[i].getName().equals(Constants.ADDED_METHOD_NAME) && !meth[i].getName().equals(Constants.ADDED_STATIC_METHOD_NAME))
               visible.add(meth[i]);
         }

         ClassData cta = cd;
         while (cta != null)
         {
            for (MethodData i : cta.getMethods())
            {
               if (i.getType() == MemberType.FAKE && AccessFlag.isPublic(i.getAccessFlags()))
               {
                  Class c = clazz.getClassLoader().loadClass(i.getClassName());
                  visible.add(i.getMethod(c));
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

   public static Method getMethod(Class clazz, String name, Class... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null)
      {

         Method meth = clazz.getMethod(name, parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
      MethodData md = cd.getMethodData(name, args);
      if (md == null)
      {
         Method meth = clazz.getMethod(name, parameters);
         return meth;
      }

      switch (md.getType())
      {
      case NORMAL:
         Method meth = clazz.getMethod(name, parameters);
         return meth;
      case FAKE:
         try
         {
            Class c = clazz.getClassLoader().loadClass(md.getClassName());
            if (md.isStatic())
            {
               meth = c.getMethod(name, parameters);
            }
            else
            {
               Class[] nparams = new Class[parameters.length + 1];
               nparams[0] = clazz;
               for (int i = 0; i < parameters.length; ++i)
               {
                  nparams[i + 1] = parameters[i];
               }
               meth = c.getMethod(name, nparams);
            }
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

   public static Method getDeclaredMethod(Class clazz, String name, Class... parameters) throws NoSuchMethodException
   {

      ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

      if (cd == null)
      {

         Method meth = clazz.getDeclaredMethod(name, parameters);
         return meth;
      }
      String args = '(' + DescriptorUtils.classArrayToDescriptorString(parameters) + ')';
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
            Class c = clazz.getClassLoader().loadClass(md.getClassName());
            if (md.isStatic())
            {
               meth = c.getMethod(name, parameters);
            }
            else
            {
               Class[] nparams = new Class[parameters.length + 1];
               nparams[0] = clazz;
               for (int i = 0; i < parameters.length; ++i)
               {
                  nparams[i + 1] = parameters[i];
               }
               meth = c.getMethod(name, nparams);
            }
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

   public static Field[] getDeclaredFields(Class clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null || 0 == 0)
         {
            return clazz.getDeclaredFields();
         }
         Field[] meth = clazz.getDeclaredFields();
         List<Field> visible = new ArrayList<Field>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            if (!meth[i].getName().equals(Constants.ADDED_FIELD_NAME))
               visible.add(meth[i]);
         }

         for (FieldData i : cd.getFields())
         {
            if (i.getMemberType() == MemberType.FAKE)
            {
               visible.add(i.getField(clazz));
            }
         }

         Field[] ret = new Field[visible.size()];
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

   public static Field[] getFields(Class clazz)
   {
      try
      {
         ClassData cd = ClassDataStore.getClassData(clazz.getClassLoader(), Descriptor.toJvmName(clazz.getName()));

         if (cd == null || 0 == 0)
         {
            return clazz.getDeclaredFields();
         }

         Field[] meth = clazz.getFields();
         List<Field> visible = new ArrayList<Field>(meth.length);
         for (int i = 0; i < meth.length; ++i)
         {
            if (!meth[i].getName().equals(Constants.ADDED_FIELD_NAME))
               visible.add(meth[i]);
         }

         ClassData cta = cd;
         while (cta != null)
         {
            for (FieldData i : cta.getFields())
            {
               if (i.getMemberType() == MemberType.FAKE && AccessFlag.isPublic(i.getAccessFlags()))
               {
                  visible.add(i.getField(clazz));
               }
            }
            cta = cta.getSuperClassInformation();
         }

         Field[] ret = new Field[visible.size()];
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

   public static Field getField(Class clazz, String name) throws NoSuchFieldException
   {
      Field meth = clazz.getField(name);
      return meth;
   }

   public static Field getDeclaredField(Class clazz, String name) throws NoSuchFieldException
   {
      Field meth = clazz.getDeclaredField(name);
      return meth;
   }

}
