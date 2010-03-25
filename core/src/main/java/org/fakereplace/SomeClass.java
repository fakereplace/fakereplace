package org.fakereplace;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

public class SomeClass
{
   public MethodInfo createConstructor(Class<?> superClass, ConstPool proxyClassConstPool) throws BadBytecode
   {
      Constructor<?> superConst = null;
      for (Constructor<?> c : superClass.getDeclaredConstructors())
      {
         if ((c.getModifiers() & Modifier.PRIVATE) == 0)
         {
            superConst = c;
            break;
         }
      }
      if (superConst == null)
      {
         throw new RuntimeException("superclass has no non private constructors");
      }

      MethodInfo ret = new MethodInfo(proxyClassConstPool, "<init>", "()V");
      Bytecode code = new Bytecode(proxyClassConstPool);
      // push this onto the stack
      code.add(Bytecode.ALOAD_0);
      for (Class<?> p : superConst.getParameterTypes())
      {
         // int char short boolean byte
         if (p.equals(int.class) || p.equals(char.class) || p.equals(short.class) || p.equals(boolean.class) || p.equals(byte.class))
         {
            // push integer 0
            code.add(Opcode.ICONST_0);
         }
         // long
         else if (p.equals(long.class))
         {
            code.add(Opcode.LCONST_0);
         }
         // double
         else if (p.equals(double.class))
         {
            code.add(Opcode.DCONST_0);
         }
         // float
         else if (p.equals(float.class))
         {
            code.add(Opcode.FCONST_0);
         }
         // arrays and reference types
         else
         {
            code.add(Opcode.ACONST_NULL);
         }
      }
      // all our args should be pushed onto the stack, call the constructor
      code.addInvokespecial(superClass.getName(), "<init>", methodSignitureToDescriptor(void.class, superConst.getParameterTypes()));
      CodeAttribute ca = code.toCodeAttribute();
      ca.computeMaxStack();
      ret.setCodeAttribute(ca);

      ret.setAccessFlags(AccessFlag.PUBLIC);

      return ret;
   }

   public static String methodSignitureToDescriptor(Class<?> returnType, Class<?>... params)
   {
      StringBuilder sb = new StringBuilder("(");
      sb.append(classArrayToDescriptorString(params));
      sb.append(")");
      sb.append(classToStringRepresentation(returnType));
      return sb.toString();
   }

   public static String classArrayToDescriptorString(Class<?>... params)
   {
      if (params == null)
      {
         return "";
      }
      StringBuilder ret = new StringBuilder();
      for (Class<?> c : params)
      {
         ret.append(classToStringRepresentation(c));
      }
      return ret.toString();
   }

   public static String classToStringRepresentation(Class<?> c)
   {
      if (void.class.equals(c))
      {
         return "V";
      }
      else if (byte.class.equals(c))
      {
         return "B";
      }
      else if (char.class.equals(c))
      {
         return "C";
      }
      else if (double.class.equals(c))
      {
         return "D";
      }
      else if (float.class.equals(c))
      {
         return "F";
      }
      else if (int.class.equals(c))
      {
         return "I";
      }
      else if (long.class.equals(c))
      {
         return "J";
      }
      else if (short.class.equals(c))
      {
         return "S";
      }
      else if (boolean.class.equals(c))
      {
         return "Z";
      }
      else if (c.isArray())
      {
         return c.getName().replace(".", "/");
      }
      else
      // normal object
      {
         return extToInt(c.getName());
      }
   }

   public static String extToInt(String className)
   {
      String repl = className.replace(".", "/");
      return 'L' + repl + ';';
   }

}
