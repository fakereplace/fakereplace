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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import org.fakereplace.logging.Logger;
import org.fakereplace.util.JumpMarker;
import org.fakereplace.util.JumpUtils;

/**
 * manipulator that replaces Method.invokewith the following:
 * <p/>
 * <code>
 * if(MethodReflection.fakeCallRequired)
 * MethodReflection.invoke
 * else
 * method.invoke
 * </code>
 *
 * @author stuart
 */
public class ReflectionMethodAccessManipulator implements ClassManipulator {

    public static final String METHOD_NAME = "invoke";
    public static final String REPLACED_METHOD_DESCRIPTOR = "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
    public static final String METHOD_DESCRIPTOR = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";

    private static final Logger log = Logger.getLogger(ReflectionMethodAccessManipulator.class);

    public void clearRewrites(String className, ClassLoader loader) {

    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
        Set<Integer> methodCallLocations = new HashSet<Integer>();
        Integer newCallLocation = null;
        Integer methodReflectionLocation = null;
        // first we need to scan the constant pool looking for
        // CONSTANT_method_info_ref structures
        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            // we have a method call
            if (pool.getTag(i) == ConstPool.CONST_Methodref) {
                String className = pool.getMethodrefClassName(i);
                String methodName = pool.getMethodrefName(i);

                if (className.equals(Method.class.getName())) {
                    if (methodName.equals("invoke")) {
                        // store the location in the const pool of the method ref
                        methodCallLocations.add(i);
                        // we have found a method call

                        // if we have not already stored a reference to our new
                        // method in the const pool
                        if (newCallLocation == null) {
                            methodReflectionLocation = pool.addClassInfo("org.fakereplace.reflection.MethodReflection");
                            newCallLocation = pool.addNameAndTypeInfo(METHOD_NAME, REPLACED_METHOD_DESCRIPTOR);
                        }
                    }
                }
            }
        }

        // this means we found an instance of the call, now we have to iterate
        // through the methods and replace instances of the call
        if (newCallLocation != null) {
            List<MethodInfo> methods = file.getMethods();
            for (MethodInfo m : methods) {
                try {
                    // ignore abstract methods
                    if (m.getCodeAttribute() == null) {
                        continue;
                    }
                    CodeIterator it = m.getCodeAttribute().iterator();
                    while (it.hasNext()) {
                        // loop through the bytecode
                        int index = it.next();
                        int op = it.byteAt(index);
                        // if the bytecode is a method invocation
                        if (op == CodeIterator.INVOKEVIRTUAL) {
                            int val = it.s16bitAt(index + 1);
                            // if the method call is one of the methods we are
                            // replacing
                            if (methodCallLocations.contains(val)) {
                                Bytecode b = new Bytecode(file.getConstPool());
                                // our stack looks like Method, instance,params
                                // we need Method, instance, params , Method
                                b.add(Opcode.DUP_X2);
                                b.add(Opcode.POP);
                                b.add(Opcode.DUP_X2);
                                b.add(Opcode.POP);
                                b.add(Opcode.DUP_X2);
                                b.addInvokestatic(methodReflectionLocation, "fakeCallRequired", "(Ljava/lang/reflect/Method;)Z");
                                b.add(Opcode.IFEQ);
                                JumpMarker performRealCall = JumpUtils.addJumpInstruction(b);
                                // now perform the fake call
                                b.addInvokestatic(methodReflectionLocation, "invoke", REPLACED_METHOD_DESCRIPTOR);
                                b.add(Opcode.GOTO);
                                JumpMarker finish = JumpUtils.addJumpInstruction(b);
                                performRealCall.mark();
                                b.addInvokevirtual(Method.class.getName(), METHOD_NAME, METHOD_DESCRIPTOR);
                                finish.mark();
                                it.writeByte(CodeIterator.NOP, index);
                                it.writeByte(CodeIterator.NOP, index + 1);
                                it.writeByte(CodeIterator.NOP, index + 2);
                                it.insertEx(b.get());

                                modifiedMethods.add(m);
                            }
                        }

                    }
                } catch (Exception e) {
                    log.error("Bad byte code transforming " + file.getName(), e);
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
