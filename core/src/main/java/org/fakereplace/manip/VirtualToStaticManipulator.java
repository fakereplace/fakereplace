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

import org.fakereplace.logging.Logger;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;

public class VirtualToStaticManipulator implements ClassManipulator {

    private final ManipulationDataStore<Data> data = new ManipulationDataStore<>();

    private final Logger log = Logger.getLogger(VirtualToStaticManipulator.class);

    public void clearRewrites(String className, ClassLoader loader) {
        data.remove(className, loader);
    }

    /**
     * This can also be used to replace a static invokation with another static
     * invokation.
     * <p>
     * if newClass is null then the invokation is changed to point to a method on the current class
     *
     */
    public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        Data d = new Data(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, null, classLoader);
        data.add(oldClass, d);
    }

    public void replaceVirtualMethodInvokationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        Data d = new Data(oldClass, null, methodName, methodDesc, newStaticMethodDesc, newMethodName, classLoader);
        data.add(oldClass, d);
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
        final Map<String, Set<Data>> virtualToStaticMethod = data.getManipulationData(loader);
        final Map<Integer, Data> methodCallLocations = new HashMap<>();
        final Map<Data, Integer> newClassPoolLocations = new HashMap<>();
        final Map<Data, Integer> newCallLocations = new HashMap<>();
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
                    for (Data data : virtualToStaticMethod.get(className)) {
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
                                Data data = methodCallLocations.get(val);
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

    private static class Data implements ClassLoaderFiltered<Data> {
        private final String oldClass;
        private final String newClass;
        private final String methodName;
        private final String newMethodName;
        private final String methodDesc;
        private final String newStaticMethodDesc;
        private final ClassLoader classLoader;

        public Data(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, String newMethodName, ClassLoader classLoader) {
            this.oldClass = oldClass;
            this.newClass = newClass;
            this.methodName = methodName;
            if (newMethodName == null) {
                this.newMethodName = methodName;
            } else {
                this.newMethodName = newMethodName;
            }
            this.methodDesc = methodDesc;
            this.newStaticMethodDesc = newStaticMethodDesc;
            this.classLoader = classLoader;
        }

        public String toString() {
            return oldClass +
                    " " +
                    newClass +
                    " " +
                    methodName +
                    " " +
                    methodDesc +
                    " " +
                    newStaticMethodDesc;
        }

        public boolean equals(Object o) {
            if (o.getClass().isAssignableFrom(Data.class)) {
                Data i = (Data) o;
                return oldClass.equals(i.oldClass) && newClass.equals(i.newClass) && methodName.equals(i.methodName) && methodDesc.equals(i.methodDesc) && newStaticMethodDesc.equals(i.newStaticMethodDesc);
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String getOldClass() {
            return oldClass;
        }

        public String getNewClass() {
            return newClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getNewMethodName() {
            return newMethodName;
        }

        public String getMethodDesc() {
            return methodDesc;
        }

        public String getNewStaticMethodDesc() {
            return newStaticMethodDesc;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public Data getInstance() {
            return this;
        }
    }
}
