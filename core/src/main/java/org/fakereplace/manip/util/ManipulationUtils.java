package org.fakereplace.manip.util;

import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.MethodData;
import org.fakereplace.util.DescriptorUtils;

/**
 * Class that holds various static helper methods that manipulate the bytecode
 * 
 * @author stuart
 * 
 */
public class ManipulationUtils
{

   private ManipulationUtils()
   {
   }

   /**
    * This class changes a block of code to return either a boxed version of a
    * Primitive type or null if the method is void
    * 
    * @author stuart
    * 
    */
   public static class MethodReturnRewriter
   {

      public static void rewriteFakeMethod(CodeIterator methodBody, String methodDescriptor)
      {
         String ret = DescriptorUtils.getReturnType(methodDescriptor);
         // if the return type is larger than one then it is not a primitive
         // so it does not need to be boxed
         if (ret.length() != 1)
         {
            return;
         }
         byte ar = (byte) Opcode.ARETURN;
         byte[] areturn = { ar };
         // void methods are special
         if (ret.equals("V"))
         {

            while (methodBody.hasNext())
            {
               try
               {
                  int index = methodBody.next();
                  int opcode = methodBody.byteAt(index);
                  // replace a RETURN opcode with
                  // ACONST_NULL
                  // ARETURN
                  // to return a null value
                  if (opcode == Opcode.RETURN)
                  {
                     Bytecode code = new Bytecode(methodBody.get().getConstPool());
                     code.add(Opcode.ACONST_NULL);
                     code.add(Opcode.ARETURN);
                     methodBody.insertAt(index, code.get());

                  }
               }
               catch (BadBytecode e)
               {
                  throw new RuntimeException(e);
               }
            }
         }
         else
         {
            while (methodBody.hasNext())
            {
               try
               {
                  int index = methodBody.next();
                  int opcode = methodBody.byteAt(index);

                  switch (opcode)
                  {
                  case Opcode.IRETURN:
                  case Opcode.LRETURN:
                  case Opcode.DRETURN:
                  case Opcode.FRETURN:
                     // write a NOP over the old return instruction
                     // insert the boxing code to get an object on the stack
                     Bytecode b = new Bytecode(methodBody.get().getConstPool());
                     Boxing.box(b, ret.charAt(0));
                     b.addOpcode(Opcode.ARETURN);
                     methodBody.insertAt(index, b.get());

                  }
               }
               catch (BadBytecode e)
               {
                  throw new RuntimeException(e);
               }
            }
         }
      }

      /**
       * Gets the correct return instruction for a proxy method
       * 
       * @param pool
       * @param methodDescriptor
       */
      public static void addReturnProxyMethod(String methodDescriptor, Bytecode b)
      {
         String ret = DescriptorUtils.getReturnType(methodDescriptor);
         // if the return type is larger than one then it is not a primitive
         // so just do an ARETURN
         if (ret.length() != 1)
         {
            b.addCheckcast(DescriptorUtils.getReturnTypeInJvmFormat(methodDescriptor));
            b.add(Opcode.ARETURN);
            return;
         }
         // void methods are special
         if (ret.equals("V"))
         {
            b.add(Opcode.RETURN);
            return;
         }
         else
         {
            // unbox the primitive type

            char tp = ret.charAt(0);
            Boxing.unbox(b, tp);
            if (tp == 'F')
            {
               b.add(Opcode.FRETURN);
            }
            else if (tp == 'D')
            {
               b.add(Opcode.DRETURN);
            }
            else if (tp == 'J')
            {
               b.add(Opcode.LRETURN);
            }
            else
            {
               b.add(Opcode.IRETURN);
            }
            return;
         }
      }
   }

   /**
    * add a bogus constructor call to a bytecode sequence so a constructor can
    * pass bytecode validation
    * 
    * @param bytecode
    */
   public static boolean addBogusConstructorCall(ClassFile file, Bytecode code)
   {
      MethodInfo constructorToCall = null;
      for (Object meth : file.getMethods())
      {
         MethodInfo m = (MethodInfo) meth;
         if (m.getName().equals("<init>"))
         {
            constructorToCall = m;
            break;
         }
      }
      if (constructorToCall == null)
      {
         return false;
      }
      // push this onto the stack
      code.add(Bytecode.ALOAD_0);

      String[] params = DescriptorUtils.descriptorStringToParameterArray(constructorToCall.getDescriptor());
      for (String p : params)
      {
         // int char short boolean byte
         if (p.equals("I") || p.equals("C") || p.equals("S") || p.equals("Z") || p.equals("B"))
         {
            // push integer 0
            code.add(Opcode.ICONST_0);
         }
         // long
         else if (p.equals("J"))
         {
            code.add(Opcode.LCONST_0);
         }
         // double
         else if (p.equals("D"))
         {
            code.add(Opcode.DCONST_0);
         }
         // float
         else if (p.equals("F"))
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
      code.addInvokespecial(file.getName(), "<init>", constructorToCall.getDescriptor());
      code.add(Opcode.RETURN);
      return true;
   }

   /**
    * inserts a 16 bit offset into the bytecode
    * 
    * @param b
    * @param value
    */
   public static void add16bit(Bytecode b, int value)
   {
      value = value % 65536;
      b.add(value >> 8);
      b.add(value % 256);
   }

   /**
    * Add a method to a class that simply delegates to the parent implementation
    * of the method
    */
   public static void addDelegatingMethod(ClassFile file, MethodData mData) throws BadBytecode, DuplicateMemberException
   {
      MethodInfo m = new MethodInfo(file.getConstPool(), mData.getMethodName(), mData.getDescriptor());
      m.setAccessFlags(mData.getAccessFlags());
      Bytecode code = new Bytecode(file.getConstPool());

      String[] params = DescriptorUtils.descriptorStringToParameterArray(mData.getDescriptor());
      code.add(Opcode.ALOAD_0); // push this
      int count = 1; // zero is the this pointer
      int maxLocals = 1;
      for (String p : params)
      {
         // int char short boolean byte
         if (p.equals("I") || p.equals("C") || p.equals("S") || p.equals("Z") || p.equals("B"))
         {
            // push integer 0
            code.addIload(count);
            maxLocals++;
         }
         // long
         else if (p.equals("J"))
         {
            code.addLload(count);
            maxLocals += 2;
            count++;
         }
         // double
         else if (p.equals("D"))
         {
            code.addDload(count);
            maxLocals += 2;
            count++;
         }
         // float
         else if (p.equals("F"))
         {
            code.addFload(count);
            maxLocals++;
         }
         // arrays and reference types
         else
         {
            code.addAload(count);
            maxLocals++;
         }
         count++;
      }
      code.addInvokespecial(mData.getClassName(), mData.getMethodName(), mData.getDescriptor());
      String p = DescriptorUtils.getReturnTypeInJvmFormat(mData.getDescriptor());
      // int char short boolean byte
      if (p.equals("I") || p.equals("C") || p.equals("S") || p.equals("Z") || p.equals("B"))
      {
         code.add(Opcode.IRETURN);
      }
      // long
      else if (p.equals("J"))
      {
         code.add(Opcode.LRETURN);
      }
      // double
      else if (p.equals("D"))
      {
         code.add(Opcode.DRETURN);
      }
      // float
      else if (p.equals("F"))
      {
         code.add(Opcode.FRETURN);
      }
      // void
      else if (p.equals("V"))
      {
         code.add(Opcode.RETURN);
      }
      // arrays and reference types
      else
      {
         code.add(Opcode.ARETURN);
      }
      CodeAttribute ca = code.toCodeAttribute();
      ca.computeMaxStack();
      ca.setMaxLocals(maxLocals);
      m.setCodeAttribute(ca);
      AttributeInfo myInfo = new AttributeInfo(file.getConstPool(), Constants.ADDED_SUPERCLASS_DELEGATING_METHOD_ATTRIBUTE, mData.getClassName().getBytes());
      m.addAttribute(myInfo);
      file.addMethod(m);
   }

   public static void pushParametersIntoArray(Bytecode bc, String methodDescriptor)
   {
      String[] params = DescriptorUtils.descriptorStringToParameterArray(methodDescriptor);
      // now we need an array:
      bc.addIconst(params.length);
      bc.addAnewarray("java.lang.Object");
      // now we have our array sitting on top of the stack
      // we need to stick our parameters into it. We do this is reverse
      // as we can't pull them from the bottom of the stack
      for (int i = params.length - 1; i >= 0; --i)
      {

         if (DescriptorUtils.isWide(params[i]))
         {
            // dup the array below the wide
            bc.add(Opcode.DUP_X2);
            // now do it again so we have two copies
            bc.add(Opcode.DUP_X2);
            // now pop it, the is the equivilent of a wide swap
            bc.add(Opcode.POP);
         }
         else
         {
            // duplicate the array to place 3
            bc.add(Opcode.DUP_X1);
            // now swap
            bc.add(Opcode.SWAP);
         }
         // now the parameter is above the array
         // box it if nessesary
         if (DescriptorUtils.isPrimitive(params[i]))
         {
            Boxing.box(bc, params[i].charAt(0));
         }
         // add the array index
         bc.addIconst(i);
         bc.add(Opcode.SWAP);
         bc.add(Opcode.AASTORE);
         // we still have the array on the top of the stack becuase we
         // duplicated it earlier
      }
   }
}
