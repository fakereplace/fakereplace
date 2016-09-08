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

import org.fakereplace.api.environment.CurrentEnvironment;
import org.fakereplace.core.Constants;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MethodData;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.data.ConstructorRewriteData;
import org.fakereplace.manip.util.ManipulationDataStore;
import org.fakereplace.manip.util.ManipulationUtils;
import org.fakereplace.runtime.MethodIdentifierStore;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

public class ConstructorInvocationManipulator implements ClassManipulator {

    private static final Logger log = Logger.getLogger(ConstructorInvocationManipulator.class);

    private final ManipulationDataStore<ConstructorRewriteData> data = new ManipulationDataStore<ConstructorRewriteData>();

    public synchronized void clearRewrites(String className, ClassLoader loader) {
        data.remove(className, loader);
    }

    /**
     * This class re-writes constructor access. It is more complex than other
     * manipulators as the work can't be hidden away in a temporary class
     */
    public void rewriteConstructorCalls(String clazz, String descriptor, int methodNo, ClassLoader classLoader) {
        ConstructorRewriteData d = new ConstructorRewriteData(clazz, descriptor, methodNo, classLoader);
        data.add(clazz, d);
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
        Map<String, Set<ConstructorRewriteData>> constructorRewrites = new HashMap<>(data.getManipulationData(loader));
        Map<Integer, ConstructorRewriteData> methodCallLocations = new HashMap<Integer, ConstructorRewriteData>();
        // first we need to scan the constant pool looking for
        // CONSTANT_method_info_ref structures
        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            // we have a method call
            if (pool.getTag(i) == ConstPool.CONST_Methodref) {
                boolean handled = false;
                String className = pool.getMethodrefClassName(i);
                String methodDesc = pool.getMethodrefType(i);
                String methodName = pool.getMethodrefName(i);
                if(methodName.equals("<init>")) {
                    if (constructorRewrites.containsKey(className)) {
                        for (ConstructorRewriteData data : constructorRewrites.get(className)) {
                            if (methodDesc.equals(data.getMethodDesc())) {
                                // store the location in the const pool of the method ref
                                methodCallLocations.put(i, data);
                                // we have found a method call
                                // now lets replace it
                                handled = true;
                                break;
                            }
                        }
                    }

                    if (!handled && CurrentEnvironment.getEnvironment().isClassReplaceable(className, loader)) {
                        //may be an added field
                        //if the field does not actually exist yet we just assume it is about to come into existence
                        //and rewrite it anyway
                        BaseClassData data = ClassDataStore.instance().getBaseClassData(loader, className);
                        if (data != null) {
                            MethodData method = data.getMethodOrConstructor("<init>", methodDesc);
                            if (method == null) {
                                //this is a new method
                                //lets deal with it
                                int methodNo = MethodIdentifierStore.instance().getMethodNumber("<init>", methodDesc);
                                methodCallLocations.put(i, new ConstructorRewriteData(className, methodDesc, methodNo, loader));
                            }
                        }
                    }
                }
            }
        }

        // this means we found an instance of the call, now we have to iterate
        // through the methods and replace instances of the call
        if (!methodCallLocations.isEmpty()) {
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
                        if (op == CodeIterator.INVOKESPECIAL) {
                            int val = it.s16bitAt(index + 1);
                            // if the method call is one of the methods we are
                            // replacing
                            if (methodCallLocations.containsKey(val)) {
                                ConstructorRewriteData data = methodCallLocations.get(val);

                                // so we currently have all the arguments sitting on the
                                // stack, and we need to jigger them into
                                // an array and then call our method. First thing to do
                                // is scribble over the existing
                                // instructions:
                                it.writeByte(CodeIterator.NOP, index);
                                it.writeByte(CodeIterator.NOP, index + 1);
                                it.writeByte(CodeIterator.NOP, index + 2);

                                Bytecode bc = new Bytecode(file.getConstPool());
                                ManipulationUtils.pushParametersIntoArray(bc, data.getMethodDesc());
                                // so now our stack looks like unconstructed instance : array
                                // we need unconstructed instance : int : array : null
                                bc.addIconst(data.getMethodNo());
                                bc.add(Opcode.SWAP);
                                bc.add(Opcode.ACONST_NULL);
                                bc.addInvokespecial(data.getClazz(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
                                // and we have our bytecode
                                it.insert(bc.get());

                            }
                        }
                    }
                    modifiedMethods.add(m);
                    m.getCodeAttribute().computeMaxStack();
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
