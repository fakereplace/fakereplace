/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.manip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.core.Constants;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.data.FakeMethodCallData;
import org.fakereplace.manip.util.Boxing;
import org.fakereplace.manip.util.ManipulationDataStore;
import org.fakereplace.manip.util.ManipulationUtils;
import org.fakereplace.util.DescriptorUtils;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * Manipulator that handles fake method call invocations
 */
public class FakeMethodCallManipulator implements ClassManipulator {

    private final ManipulationDataStore<FakeMethodCallData> data = new ManipulationDataStore<>();

    private final Logger log = Logger.getLogger(FakeMethodCallManipulator.class);

    public void clearRewrites(String className, ClassLoader loader) {
        data.remove(className, loader);
    }

    public void addFakeMethodCall(FakeMethodCallData methodInfo) {
        data.add(methodInfo.getClassName(), methodInfo);
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
        final Map<String, Set<FakeMethodCallData>> virtualToStaticMethod = data.getManipulationData(loader);
        final Map<Integer, FakeMethodCallData> methodCallLocations = new HashMap<>();
        final Map<FakeMethodCallData, Integer> newClassPoolLocations = new HashMap<>();
        final Map<FakeMethodCallData, Integer> newCallLocations = new HashMap<>();
        // first we need to scan the constant pool looking for
        // CONSTANT_method_info_ref structures
        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            // we have a method call
            if (pool.getTag(i) == ConstPool.CONST_Methodref || pool.getTag(i) == ConstPool.CONST_InterfaceMethodref) {
                String className, methodDesc, methodName;
                if (pool.getTag(i) == ConstPool.CONST_Methodref) {
                    className = pool.getMethodrefClassName(i);
                    methodDesc = pool.getMethodrefType(i);
                    methodName = pool.getMethodrefName(i);
                } else {
                    className = pool.getInterfaceMethodrefClassName(i);
                    methodDesc = pool.getInterfaceMethodrefType(i);
                    methodName = pool.getInterfaceMethodrefName(i);
                }

                if (virtualToStaticMethod.containsKey(className)) {
                    for (FakeMethodCallData data : virtualToStaticMethod.get(className)) {
                        if (methodName.equals(data.getMethodName()) && methodDesc.equals(data.getMethodDesc())) {
                            // store the location in the const pool of the method ref
                            methodCallLocations.put(i, data);
                            // we have found a method call
                            // now lets replace it

                            // if we have not already stored a reference to our new
                            // method in the const pool
                            if (!newClassPoolLocations.containsKey(data)) {
                                // we have not added the new class reference or
                                // the new call location to the class pool yet
                                int newCpLoc;
                                if (data.getClassName() != null) {
                                    newCpLoc = pool.addClassInfo(data.getClassName());
                                } else {
                                    newCpLoc = pool.addClassInfo(file.getName());
                                }
                                newClassPoolLocations.put(data, newCpLoc);
                                int newNameAndType;
                                if (data.getType() == FakeMethodCallData.Type.STATIC) {
                                    newNameAndType = pool.addNameAndTypeInfo(Constants.ADDED_STATIC_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
                                } else {
                                    newNameAndType = pool.addNameAndTypeInfo(Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
                                }
                                newCallLocations.put(data, pool.addMethodrefInfo(newCpLoc, newNameAndType));

                            }
                            break;
                        }

                    }
                }
            }
        }

        // this means we found an instance of the call, now we have to iterate
        // through the methods and replace instances of the call
        if (!newClassPoolLocations.isEmpty()) {
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
                        if (op == CodeIterator.INVOKEVIRTUAL || op == CodeIterator.INVOKESTATIC || op == CodeIterator.INVOKEINTERFACE || op == CodeIterator.INVOKESPECIAL) {
                            int val = it.s16bitAt(index + 1);
                            // if the method call is one of the methods we are
                            // replacing
                            if (methodCallLocations.containsKey(val)) {
                                FakeMethodCallData data = methodCallLocations.get(val);
                                //NOP out the whole thing
                                it.writeByte(CodeIterator.NOP, index );
                                it.writeByte(CodeIterator.NOP, index + 1);
                                it.writeByte(CodeIterator.NOP, index + 2);
                                if (op == CodeIterator.INVOKEINTERFACE) {
                                    // INVOKEINTERFACE has some extra parameters
                                    it.writeByte(CodeIterator.NOP, index + 3);
                                    it.writeByte(CodeIterator.NOP, index + 4);
                                }
                                //now we write some bytecode to invoke it directly
                                final boolean staticMethod = data.getType() == FakeMethodCallData.Type.STATIC;
                                Bytecode byteCode = new Bytecode(file.getConstPool());

                                // stick the method number in the const pool then load it onto the
                                // stack

                                String[] types = DescriptorUtils.descriptorStringToParameterArray(data.getMethodDesc());
                                // create a new array the same size as the parameter array
                                int typesLength = byteCode.getConstPool().addIntegerInfo(types.length);
                                byteCode.addLdc(typesLength);
                                // create new array to use to pass our parameters
                                byteCode.addAnewarray("java.lang.Object");
                                for (int i = types.length - 1; i >= 0; --i) {
                                    // duplicate the array reference on the stack
                                    boolean wide = DescriptorUtils.isWide(types[i]);
                                    if(wide) {
                                        byteCode.add(Opcode.DUP_X2);
                                        byteCode.add(Opcode.DUP_X2);
                                        byteCode.add(Opcode.POP);
                                        // load the array index into the stack
                                        byteCode.addLdc(byteCode.getConstPool().addIntegerInfo(i));
                                        byteCode.add(Opcode.DUP_X2);
                                        byteCode.add(Opcode.POP);
                                    } else {
                                        byteCode.add(Opcode.DUP_X1);
                                        byteCode.add(Opcode.SWAP);
                                        // load the array index into the stack
                                        byteCode.addLdc(byteCode.getConstPool().addIntegerInfo(i));
                                        byteCode.add(Opcode.SWAP);
                                    }

                                    if(DescriptorUtils.isPrimitive(types[i])) {
                                        Boxing.box(byteCode, types[i].charAt(0));
                                    }
                                    byteCode.add(Opcode.AASTORE);// store the value in the array
                                }

                                int scind = file.getConstPool().addIntegerInfo(data.getMethodNumber());
                                byteCode.addLdc(scind);
                                byteCode.add(Opcode.SWAP);
                                // invoke the added method
                                if (staticMethod) {
                                    byteCode.addInvokestatic(data.getClassName(), Constants.ADDED_STATIC_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
                                } else if (data.getType() == FakeMethodCallData.Type.INTERFACE) {
                                    byteCode.addInvokeinterface(data.getClassName(), Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;", 3);
                                } else {
                                    byteCode.addInvokevirtual(data.getClassName(), Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
                                }
                                // cast it to the appropriate type and return it
                                String returnType = DescriptorUtils.getReturnType(data.getMethodDesc());
                                if(returnType.length() == 1 && !returnType.equals("V")) {
                                    Boxing.unbox(byteCode, returnType.charAt(0));
                                } else if(returnType.equals("V")) {
                                    byteCode.add(Opcode.POP);
                                } else {
                                    byteCode.addCheckcast(returnType.substring(1, returnType.length() - 1));
                                }
                                it.insertEx(byteCode.get());
                                modifiedMethods.add(m);
                            }
                        }
                    }
                    modifiedMethods.add(m);
                    m.getCodeAttribute().computeMaxStack();
                } catch (Exception e) {
                    log.error("Bad byte code transforming " + file.getName(), e);
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
