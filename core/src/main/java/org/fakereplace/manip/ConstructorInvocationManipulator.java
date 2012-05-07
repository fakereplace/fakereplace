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

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import org.fakereplace.core.Constants;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.data.ConstructorRewriteData;
import org.fakereplace.manip.util.ManipulationDataStore;
import org.fakereplace.manip.util.ManipulationUtils;

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

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass) {
        Map<String, Set<ConstructorRewriteData>> constructorRewrites = data.getManipulationData(loader);
        if (constructorRewrites.isEmpty()) {
            return false;
        }
        Map<Integer, ConstructorRewriteData> methodCallLocations = new HashMap<Integer, ConstructorRewriteData>();
        Integer newCallLocation = null;
        // first we need to scan the constant pool looking for
        // CONSTANT_method_info_ref structures
        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            // we have a method call
            if (pool.getTag(i) == ConstPool.CONST_Methodref) {
                if (constructorRewrites.containsKey(pool.getMethodrefClassName(i))) {
                    for (ConstructorRewriteData data : constructorRewrites.get(pool.getMethodrefClassName(i))) {
                        if (pool.getMethodrefName(i).equals("<init>") && pool.getMethodrefType(i).equals(data.getMethodDesc())) {
                            // store the location in the const pool of the method ref
                            methodCallLocations.put(i, data);
                            // we have found a method call
                            // now lets replace it

                            // if we have not already stored a reference to the
                            // refinied constructor
                            // in the const pool
                            if (newCallLocation == null) {
                                // we have not added the new class reference or
                                // the new call location to the class pool yet
                                int classIndex = pool.getMethodrefClass(i);

                                int newNameAndType = pool.addNameAndTypeInfo("<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
                                newCallLocation = pool.addMethodrefInfo(classIndex, newNameAndType);
                            }
                            break;
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
                                // so now our stack looks like unconstructed instance :
                                // array
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
