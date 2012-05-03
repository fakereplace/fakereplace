/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
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
import org.fakereplace.boot.Logger;
import org.fakereplace.manip.data.VirtualToStaticData;
import org.fakereplace.manip.util.ManipulationDataStore;

public class MethodInvokationManipulator implements ClassManipulator {
    ManipulationDataStore<VirtualToStaticData> data = new ManipulationDataStore<VirtualToStaticData>();

    public void clearRewrites(String className, ClassLoader loader) {
        data.remove(className, loader);
    }

    /**
     * This can also be used to replace a static invokation with another static
     * invokation.
     * <p/>
     * if newClass is null then the invokation is changed to point to a method on the current class
     *
     * @param oldClass
     * @param newClass
     * @param methodName
     * @param methodDesc
     * @param newStaticMethodDesc
     */
    public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        VirtualToStaticData d = new VirtualToStaticData(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, null, classLoader);
        data.add(oldClass, d);
    }

    public void replaceVirtualMethodInvokationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        VirtualToStaticData d = new VirtualToStaticData(oldClass, null, methodName, methodDesc, newStaticMethodDesc, newMethodName, classLoader);
        data.add(oldClass, d);
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass) {
        final Map<String, Set<VirtualToStaticData>> virtualToStaticMethod = data.getManipulationData(loader);
        final Map<Integer, VirtualToStaticData> methodCallLocations = new HashMap<Integer, VirtualToStaticData>();
        final Map<VirtualToStaticData, Integer> newClassPoolLocations = new HashMap<VirtualToStaticData, Integer>();
        final Map<VirtualToStaticData, Integer> newCallLocations = new HashMap<VirtualToStaticData, Integer>();
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
                            }
                        }

                    }
                    m.getCodeAttribute().computeMaxStack();
                } catch (Exception e) {
                    Logger.log(this, "Bad byte code transforming " + file.getName());
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
