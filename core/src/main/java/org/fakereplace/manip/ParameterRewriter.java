package org.fakereplace.manip;

import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Opcode;

import org.fakereplace.util.DescriptorUtils;

/**
 * This class is responsible for mangling local variables references in a fake
 * method to make sure they line up
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class ParameterRewriter
{
   public static void mangleParameters(boolean staticMethod, CodeAttribute attribute, String methodSigniture, int existingLocalVaraiables)
   {
      try
      {
         int offset = 0;
         String[] data = DescriptorUtils.descriptorStringToParameterArray(methodSigniture);
         if (!staticMethod)
         {
            // non static methods have a this pointer as the first argument
            // which should not be mangled
            offset = 1;
         }

         // insert two new local variables, these are the fake method parameters
         attribute.insertLocalVar(offset, 1);
         attribute.insertLocalVar(offset, 1);
         Bytecode code = new Bytecode(attribute.getConstPool());
         int varpos = offset + 2;
         for (int i = 0; i < data.length; ++i)
         {
            // push the parameter array onto the stack
            if (staticMethod)
            {
               code.add(Opcode.ALOAD_1);
            }
            else
            {
               code.add(Opcode.ALOAD_2);
            }
            int index = attribute.getConstPool().addIntegerInfo(i);
            code.addLdc(index);
            code.add(Opcode.AALOAD);
            // now we have the parameter on the stack.
            // what happens next depends on the type
            switch (data[i].charAt(0))
            {
            case 'L':
               // add a checkcast substring is to get rid of the L
               code.addCheckcast(data[i].substring(1));
               // now stick it into its proper local variable
               code.addAstore(varpos);
               varpos++;
               break;
            case '[':
               code.addCheckcast(data[i]);
               // now stick it into its proper local variable
               code.addAstore(varpos);
               varpos++;
               break;
            case 'I':
               // integer, we need to unbox it
               Boxing.unboxInt(code);
               code.addIstore(varpos);
               varpos++;
               break;
            case 'S':
               // short, we need to unbox it
               Boxing.unboxShort(code);
               code.addIstore(varpos);
               varpos++;
               break;
            case 'B':
               // short, we need to unbox it
               Boxing.unboxByte(code);
               code.addIstore(varpos);
               varpos++;
               break;
            case 'J':
               // long, we need to unbox it
               Boxing.unboxLong(code);
               code.addLstore(varpos);
               varpos = varpos + 2;
               break;
            case 'F':
               Boxing.unboxFloat(code);
               code.addFstore(varpos);
               varpos++;
               break;
            case 'D':
               Boxing.unboxDouble(code);
               code.addDstore(varpos);
               varpos++;
               break;
            case 'C':
               Boxing.unboxChar(code);
               code.addIstore(varpos);
               varpos++;
               break;
            case 'Z':
               Boxing.unboxBoolean(code);
               code.addIstore(varpos);
               varpos++;
               break;
            }

         }
         attribute.iterator().insert(0, code.get());

      }
      catch (Exception e)
      {

      }

   }
}
