package org.fakereplace.manip.util;

import javassist.bytecode.Bytecode;
import javassist.bytecode.Opcode;

/**
 * This class is responsible for generating bytecode fragments to box/unbox
 * whatever happens to be on the top of the stack.
 * 
 * It is the calling codes responsibility to make sure that the correct type is
 * on the stack
 * 
 * @author stuart
 * 
 */
public class Boxing
{

   static public void box(Bytecode b, char type)
   {
      switch (type)
      {
      case 'I':
         boxInt(b);
         break;
      case 'J':
         boxLong(b);
         break;
      case 'S':
         boxShort(b);
         break;
      case 'F':
         boxFloat(b);
         break;
      case 'D':
         boxDouble(b);
         break;
      case 'B':
         boxByte(b);
         break;
      case 'C':
         boxChar(b);
         break;
      case 'Z':
         boxBoolean(b);
         break;
      default:
         throw new RuntimeException("Cannot box unkown primitive type: " + type);
      }

   }

   static public Bytecode unbox(Bytecode b, char type)
   {
      switch (type)
      {
      case 'I':
         return unboxInt(b);
      case 'J':
         return unboxLong(b);
      case 'S':
         return unboxShort(b);
      case 'F':
         return unboxFloat(b);
      case 'D':
         return unboxDouble(b);
      case 'B':
         return unboxByte(b);
      case 'C':
         return unboxChar(b);
      case 'Z':
         return unboxBoolean(b);
      }
      throw new RuntimeException("Cannot unbox unkown primitive type: " + type);
   }

   static public void boxInt(Bytecode bc)
   {
      bc.addNew("java.lang.Integer");
      bc.add(Opcode.DUP_X1);
      bc.add(Opcode.SWAP);
      bc.addInvokespecial("java.lang.Integer", "<init>", "(I)V");
   }

   static public void boxLong(Bytecode bc)
   {
      bc.addNew("java.lang.Long");
      bc.add(Opcode.DUP_X2);
      bc.add(Opcode.DUP_X2);
      bc.add(Opcode.POP);
      bc.addInvokespecial("java.lang.Long", "<init>", "(J)V");
   }

   static public void boxShort(Bytecode bc)
   {
      bc.addNew("java.lang.Short");
      bc.add(Opcode.DUP_X1);
      bc.add(Opcode.SWAP);
      bc.addInvokespecial("java.lang.Short", "<init>", "(S)V");
   }

   static public void boxByte(Bytecode bc)
   {
      bc.addNew("java.lang.Byte");
      bc.add(Opcode.DUP_X1);
      bc.add(Opcode.SWAP);
      bc.addInvokespecial("java.lang.Byte", "<init>", "(B)V");
   }

   static public void boxFloat(Bytecode bc)
   {
      bc.addNew("java.lang.Float");
      bc.add(Opcode.DUP_X1);
      bc.add(Opcode.SWAP);
      bc.addInvokespecial("java.lang.Float", "<init>", "(F)V");
   }

   static public void boxDouble(Bytecode bc)
   {
      bc.addNew("java.lang.Double");
      bc.add(Opcode.DUP_X2);
      bc.add(Opcode.DUP_X2);
      bc.add(Opcode.POP);
      bc.addInvokespecial("java.lang.Double", "<init>", "(D)V");
   }

   static public void boxChar(Bytecode bc)
   {
      bc.addNew("java.lang.Character");
      bc.add(Opcode.DUP_X1);
      bc.add(Opcode.SWAP);
      bc.addInvokespecial("java.lang.Character", "<init>", "(C)V");
   }

   static public void boxBoolean(Bytecode bc)
   {
      bc.addNew("java.lang.Boolean");
      bc.add(Opcode.DUP_X1);
      bc.add(Opcode.SWAP);
      bc.addInvokespecial("java.lang.Boolean", "<init>", "(Z)V");
   }

   // unboxing

   static public Bytecode unboxInt(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Number");
      bc.addInvokevirtual("java.lang.Number", "intValue", "()I");
      return bc;
   }

   static public Bytecode unboxLong(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Number");
      bc.addInvokevirtual("java.lang.Number", "longValue", "()J");
      return bc;
   }

   static public Bytecode unboxShort(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Number");
      bc.addInvokevirtual("java.lang.Number", "shortValue", "()S");
      return bc;
   }

   static public Bytecode unboxByte(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Number");
      bc.addInvokevirtual("java.lang.Number", "byteValue", "()B");
      return bc;
   }

   static public Bytecode unboxFloat(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Number");
      bc.addInvokevirtual("java.lang.Number", "floatValue", "()F");
      return bc;
   }

   static public Bytecode unboxDouble(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Number");
      bc.addInvokevirtual("java.lang.Number", "doubleValue", "()D");
      return bc;
   }

   static public Bytecode unboxChar(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Character");
      bc.addInvokevirtual("java.lang.Character", "charValue", "()C");
      return bc;
   }

   static public Bytecode unboxBoolean(Bytecode bc)
   {
      bc.addCheckcast("java.lang.Boolean");
      bc.addInvokevirtual("java.lang.Boolean", "booleanValue", "()Z");
      return bc;
   }

}
