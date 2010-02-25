package org.fakereplace.data;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.fakereplace.util.AnnotationInstanceProvider;

public class AnnotationBuilder
{
   public static java.lang.annotation.Annotation createAnnotation(ClassLoader loader, AnnotationInstanceProvider provider, javassist.bytecode.annotation.Annotation a)
   {
      try
      {
         Class annotationType = loader.loadClass(a.getTypeName());

         Map<String, Object> memberValues = new HashMap<String, Object>();
         Set members = a.getMemberNames();
         if (members != null)
         {
            for (Object mn : members)
            {
               String memName = mn.toString();
               MemberValue value = a.getMemberValue(memName);
               MemberValueVisitorImpl visitor = new MemberValueVisitorImpl(loader, provider);
               value.accept(visitor);
               memberValues.put(memName, visitor.getValue());
            }
         }
         return provider.get(annotationType, memberValues);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static javassist.bytecode.annotation.Annotation createJavassistAnnotation(java.lang.annotation.Annotation annotation, ConstPool cp)
   {
      try
      {
         javassist.bytecode.annotation.Annotation a = new Annotation(annotation.annotationType().getName(), cp);
         for (Method m : annotation.annotationType().getDeclaredMethods())
         {
            Object val = m.invoke(annotation);
            a.addMemberValue(m.getName(), createMemberValue(m.getReturnType(), val, cp));
         }
         return a;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   static MemberValue createMemberValue(Class type, Object val, ConstPool cp)
   {
      if (type == int.class)
      {
         return new IntegerMemberValue(cp, (Integer) val);
      }
      else if (type == short.class)
      {
         return new ShortMemberValue((Short) val, cp);
      }
      else if (type == long.class)
      {
         return new LongMemberValue((Long) val, cp);
      }
      else if (type == byte.class)
      {
         return new ByteMemberValue((Byte) val, cp);
      }
      else if (type == float.class)
      {
         return new FloatMemberValue((Float) val, cp);
      }
      else if (type == double.class)
      {
         return new DoubleMemberValue((Double) val, cp);
      }
      else if (type == char.class)
      {
         return new CharMemberValue((Character) val, cp);
      }
      else if (type == boolean.class)
      {
         return new BooleanMemberValue((Boolean) val, cp);
      }
      else if (type == String.class)
      {
         return new StringMemberValue((String) val, cp);
      }
      else if (type == Class.class)
      {
         return new ClassMemberValue(((Class) val).getName(), cp);
      }
      else if (type.isEnum())
      {
         EnumMemberValue e = new EnumMemberValue(cp);
         e.setType(type.getName());
         e.setValue(((Enum) val).name());
         return e;
      }
      else if (type.isAnnotation())
      {
         return new AnnotationMemberValue(createJavassistAnnotation((java.lang.annotation.Annotation) val, cp), cp);
      }
      else if (type.isArray())
      {
         Class arrayType = type.getComponentType();
         int length = Array.getLength(val);
         ArrayMemberValue ret = new ArrayMemberValue(createMemberValue(arrayType, null, cp), cp);
         MemberValue[] vals = new MemberValue[length];
         for (int i = 0; i < length; ++i)
         {
            vals[i] = createMemberValue(arrayType, Array.get(val, i), cp);
         }
         ret.setValue(vals);
         return ret;
      }
      throw new RuntimeException("Invalid array type " + type + " value: " + val);

   }
}
