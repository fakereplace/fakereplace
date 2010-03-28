package org.fakereplace.replacement;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.util.DescriptorUtils;

/**
 * 
 * @author stuart
 *
 */
public class FakeConstructorUtils
{
   /**
    * add a bogus constructor call to a bytecode sequence so a constructor can pass bytecode validation
    * @param bytecode
    */
   public static void addBogusConstructorCall(ClassFile file, Bytecode code)
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
         // this should not happed
         return;
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
   }

}
