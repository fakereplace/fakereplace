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

import java.util.Map;
import java.util.Set;

import org.fakereplace.runtime.VirtualDelegator;
import org.fakereplace.util.DescriptorUtils;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * this manipulator adds code that looks like:
 * <p>
 * if(!this.getClass().getName().equals("CurrentClass")) //if this is a subclass
 * {
 * if(org.fakereplace.runtime.VirtualDelegator.contains(this,methodName,
 * methodDescriptor))
 * {
 * return org.fakereplace.runtime.VirtualDelegator.run(this,methodName,
 * methodDescriptor));
 * }
 * }
 * <p>
 * to a class
 *
 * @author stuart
 */
class SubclassVirtualCallManipulator implements ClassManipulator {

    private final ManipulationDataStore<Data> data = new ManipulationDataStore<>();

    void addClassData(String className, ClassLoader classLoader, String parentClassName, ClassLoader parentClassLoader, String methodName, String methodDesc) {
        data.add(parentClassName, new Data(parentClassLoader, parentClassName, methodName, methodDesc));
        VirtualDelegator.add(classLoader, className, methodName, methodDesc);
    }

    public void clearRewrites(String className, ClassLoader classLoader) {
        // we don't need to clear them. This is handled by clearing the data in
        // VirtualDelegator
        VirtualDelegator.clear(classLoader, className);
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
        boolean modified = false;
        Map<String, Set<Data>> loaderData = data.getManipulationData(loader);
        if (loaderData.containsKey(file.getName())) {
            Set<Data> d = loaderData.get(file.getName());
            for (Data s : d) {
                for (Object m : file.getMethods()) {
                    MethodInfo method = (MethodInfo) m;
                    if (method.getName().equals(s.getMethodName()) && method.getDescriptor().equals(s.getMethodDesc())) {

                        modified = true;

                        // we have the method
                        // lets append our code to the top
                        // first create the stuff inside the coditionals

                        Bytecode run = new Bytecode(file.getConstPool());
                        run.add(Opcode.ALOAD_0);
                        run.addLdc(method.getName());
                        run.addLdc(method.getDescriptor());
                        String[] params = DescriptorUtils.descriptorStringToParameterArray(method.getDescriptor());
                        int count = 1;
                        for (int i = 0; i < params.length; ++i) {
                            if (params[i].length() > 1) {
                                run.addAload(count);
                            } else if (params[i].equals("I") || params[i].equals("Z") || params[i].equals("S") || params[i].equals("B")) {
                                run.addIload(count);
                            } else if (params[i].equals("F")) {
                                run.addFload(count);
                            } else if (params[i].equals("J")) {
                                run.addLload(count);
                                count++;
                            } else if (params[i].equals("D")) {
                                run.addDload(count);
                                count++;
                            }
                            count++;
                        }
                        ManipulationUtils.pushParametersIntoArray(run, method.getDescriptor());
                        run.addInvokestatic(VirtualDelegator.class.getName(), "run", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
                        ManipulationUtils.MethodReturnRewriter.addReturnProxyMethod(method.getDescriptor(), run);

                        Bytecode cd = new Bytecode(file.getConstPool());
                        cd.add(Opcode.ALOAD_0);
                        cd.addLdc(file.getName());
                        cd.addLdc(method.getName());
                        cd.addLdc(method.getDescriptor());
                        cd.addInvokestatic(VirtualDelegator.class.getName(), "contains", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z");
                        cd.add(Opcode.IFEQ); // if contains is true
                        ManipulationUtils.add16bit(cd, run.getSize() + 3);

                        Bytecode b = new Bytecode(file.getConstPool());
                        // this.getClass()
                        b.add(Opcode.ALOAD_0);
                        b.addInvokevirtual("java.lang.Object", "getClass", "()Ljava/lang/Class;");
                        b.addInvokevirtual("java.lang.Class", "getName", "()Ljava/lang/String;");
                        // now we have the class name on the stack
                        // push the class being manipulateds name onto the stack
                        b.addLdc(file.getName());
                        b.addInvokevirtual("java.lang.Object", "equals", "(Ljava/lang/Object;)Z");
                        // now we have a boolean on top of the stack
                        b.add(Opcode.IFNE); // if true jump
                        ManipulationUtils.add16bit(b, run.getSize() + cd.getSize() + 3);

                        try {
                            method.getCodeAttribute().iterator().insert(run.get());
                            method.getCodeAttribute().iterator().insert(cd.get());
                            method.getCodeAttribute().iterator().insert(b.get());
                            modifiedMethods.add(method);
                        } catch (BadBytecode e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return modified;
    }

    private static class Data implements ClassLoaderFiltered<Data> {

        private final ClassLoader classLoader;
        private final String className;
        private final String methodName;
        private final String methodDesc;

        public Data(ClassLoader classLoader, String className, String methodName, String methodDesc) {
            this.classLoader = classLoader;
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        public Data getInstance() {
            return this;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getMethodDesc() {
            return methodDesc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((className == null) ? 0 : className.hashCode());
            result = prime * result + ((methodDesc == null) ? 0 : methodDesc.hashCode());
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Data other = (Data) obj;
            if (classLoader == null) {
                if (other.classLoader != null)
                    return false;
            } else if (!classLoader.equals(other.classLoader))
                return false;
            if (className == null) {
                if (other.className != null)
                    return false;
            } else if (!className.equals(other.className))
                return false;
            if (methodDesc == null) {
                if (other.methodDesc != null)
                    return false;
            } else if (!methodDesc.equals(other.methodDesc))
                return false;
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;
            return true;
        }

    }
}
