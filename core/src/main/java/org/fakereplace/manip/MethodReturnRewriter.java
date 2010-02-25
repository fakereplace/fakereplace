package org.fakereplace.manip;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;

import org.fakereplace.util.DescriptorUtils;

/**
 * This class changes a block of code to return either a boxed version of a
 * Primitive type or null if the method is void
 * 
 * @author stuart
 * 
 */
public class MethodReturnRewriter
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
