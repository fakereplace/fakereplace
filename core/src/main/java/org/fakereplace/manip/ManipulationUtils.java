/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.fakereplace.manip;

import org.fakereplace.util.Boxing;
import org.fakereplace.util.DescriptorUtils;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * Class that holds various static helper methods that manipulate the bytecode
 *
 * @author stuart
 */
public class ManipulationUtils {

    private ManipulationUtils() {
    }

    /**
     * This class changes a block of code to return either a boxed version of a
     * Primitive type or null if the method is void
     *
     * @author stuart
     */
    public static class MethodReturnRewriter {


        /**
         * Gets the correct return instruction for a proxy method
         *
         */
        public static void addReturnProxyMethod(String methodDescriptor, Bytecode b) {
            String ret = DescriptorUtils.getReturnType(methodDescriptor);
            // if the return type is larger than one then it is not a primitive
            // so just do an ARETURN
            if (ret.length() != 1) {
                b.addCheckcast(DescriptorUtils.getReturnTypeInJvmFormat(methodDescriptor));
                b.add(Opcode.ARETURN);
                return;
            }
            // void methods are special
            if (ret.equals("V")) {
                b.add(Opcode.RETURN);
                return;
            } else {
                // unbox the primitive type

                char tp = ret.charAt(0);
                Boxing.unbox(b, tp);
                if (tp == 'F') {
                    b.add(Opcode.FRETURN);
                } else if (tp == 'D') {
                    b.add(Opcode.DRETURN);
                } else if (tp == 'J') {
                    b.add(Opcode.LRETURN);
                } else {
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
     */
    public static boolean addBogusConstructorCall(ClassFile file, Bytecode code) {
        MethodInfo constructorToCall = null;
        for (Object meth : file.getMethods()) {
            MethodInfo m = (MethodInfo) meth;
            if (m.getName().equals("<init>")) {
                constructorToCall = m;
                break;
            }
        }
        if (constructorToCall == null) {
            return false;
        }
        // push this onto the stack
        code.add(Bytecode.ALOAD_0);

        String[] params = DescriptorUtils.descriptorStringToParameterArray(constructorToCall.getDescriptor());
        for (String p : params) {
            // int char short boolean byte
            switch (p) {
                case "I":
                case "C":
                case "S":
                case "Z":
                case "B":
                    // push integer 0
                    code.add(Opcode.ICONST_0);
                    break;
                // long
                case "J":
                    code.add(Opcode.LCONST_0);
                    break;
                // double
                case "D":
                    code.add(Opcode.DCONST_0);
                    break;
                // float
                case "F":
                    code.add(Opcode.FCONST_0);
                    break;
                // arrays and reference types
                default:
                    code.add(Opcode.ACONST_NULL);
                    break;
            }
        }
        // all our args should be pushed onto the stack, call the constructor
        code.addInvokespecial(file.getName(), "<init>", constructorToCall.getDescriptor());
        code.addNew(NoSuchMethodError.class.getName());
        code.add(Opcode.DUP);
        code.addInvokespecial(NoSuchMethodError.class.getName(), "<init>", "()V");
        code.add(Opcode.ATHROW);
        return true;
    }

    /**
     * inserts a 16 bit offset into the bytecode
     *
     */
    public static void add16bit(Bytecode b, int value) {
        value = value % 65536;
        b.add(value >> 8);
        b.add(value % 256);
    }

    public static void pushParametersIntoArray(Bytecode bc, String methodDescriptor) {
        String[] params = DescriptorUtils.descriptorStringToParameterArray(methodDescriptor);
        // now we need an array:
        bc.addIconst(params.length);
        bc.addAnewarray("java.lang.Object");
        // now we have our array sitting on top of the stack
        // we need to stick our parameters into it. We do this is reverse
        // as we can't pull them from the bottom of the stack
        for (int i = params.length - 1; i >= 0; --i) {

            if (DescriptorUtils.isWide(params[i])) {
                // dup the array below the wide
                bc.add(Opcode.DUP_X2);
                // now do it again so we have two copies
                bc.add(Opcode.DUP_X2);
                // now pop it, the is the equivalent of a wide swap
                bc.add(Opcode.POP);
            } else {
                // duplicate the array to place 3
                bc.add(Opcode.DUP_X1);
                // now swap
                bc.add(Opcode.SWAP);
            }
            // now the parameter is above the array
            // box it if nessesary
            if (DescriptorUtils.isPrimitive(params[i])) {
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
