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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import org.fakereplace.api.environment.CurrentEnvironment;
import org.fakereplace.core.Transformer;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.MethodData;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.data.AddedFieldData;
import org.fakereplace.manip.data.VirtualToStaticData;
import org.fakereplace.manip.util.ManipulationDataStore;
import org.fakereplace.runtime.FieldReferenceDataStore;
import org.fakereplace.runtime.MethodIdentifierStore;

public class VirtualToStaticManipulator implements ClassManipulator {

    private final ManipulationDataStore<VirtualToStaticData> data = new ManipulationDataStore<VirtualToStaticData>();

    private final Logger log = Logger.getLogger(VirtualToStaticManipulator.class);

    public void clearRewrites(String className, ClassLoader loader) {
        data.remove(className, loader);
    }

    /**
     * This can also be used to replace a static invocation with another static invocation.
     * <p>
     * if newClass is null then the invocation is changed to point to a method on the current class
     */
    public void replaceVirtualMethodInvocationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        VirtualToStaticData d = new VirtualToStaticData(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, null, classLoader);
        data.add(oldClass, d);
    }

    public void replaceVirtualMethodInvocationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        VirtualToStaticData d = new VirtualToStaticData(oldClass, null, methodName, methodDesc, newStaticMethodDesc, newMethodName, classLoader);
        data.add(oldClass, d);
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
        final Map<String, Set<VirtualToStaticData>> virtualToStaticMethod = data.getManipulationData(loader);
        final Map<Integer, VirtualToStaticData> methodCallLocations = new HashMap<>();
        final Map<VirtualToStaticData, Integer> newClassPoolLocations = new HashMap<>();
        final Map<VirtualToStaticData, Integer> newCallLocations = new HashMap<>();
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
                boolean handled = false;
                if (virtualToStaticMethod.containsKey(className)) {
                    for (VirtualToStaticData data : virtualToStaticMethod.get(className)) {
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
                                if (data.getNewClass() != null) {
                                    newCpLoc = pool.addClassInfo(data.getNewClass());
                                } else {

                                    newCpLoc = pool.addClassInfo(file.getName());
                                }
                                newClassPoolLocations.put(data, newCpLoc);
                                int newNameAndType = pool.addNameAndTypeInfo(data.getNewMethodName(), data.getNewStaticMethodDesc());
                                newCallLocations.put(data, pool.addMethodrefInfo(newCpLoc, newNameAndType));

                            }
                            handled = true;
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
                                VirtualToStaticData data = methodCallLocations.get(val);
                                // change the call to an invokestatic
                                it.writeByte(CodeIterator.INVOKESTATIC, index);
                                // change the method that is being called
                                it.write16bit(newCallLocations.get(data), index + 1);
                                if (op == CodeIterator.INVOKEINTERFACE) {
                                    // INVOKEINTERFACE has some extra parameters
                                    it.writeByte(CodeIterator.NOP, index + 3);
                                    it.writeByte(CodeIterator.NOP, index + 4);
                                }
                                modifiedMethods.add(m);
                            }
                        }

                    }
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
