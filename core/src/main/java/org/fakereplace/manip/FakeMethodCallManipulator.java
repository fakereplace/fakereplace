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

import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.core.Constants;
import org.fakereplace.core.Fakereplace;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MethodData;
import org.fakereplace.logging.Logger;
import org.fakereplace.runtime.MethodIdentifierStore;
import org.fakereplace.util.Boxing;
import org.fakereplace.util.DescriptorUtils;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.BootstrapMethodsAttribute;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * Manipulator that handles fake method call invocations
 */
public class FakeMethodCallManipulator implements ClassManipulator {

    private final ManipulationDataStore<Data> data = new ManipulationDataStore<>();

    private final Logger log = Logger.getLogger(FakeMethodCallManipulator.class);

    public void clearRewrites(String className, ClassLoader loader) {
        data.remove(className, loader);
    }

    void addFakeMethodCall(String className, String methodName, String methodDesc, Type type, ClassLoader classLoader, int methodNumber, String proxyName) {
        data.add(className, new Data(className, methodName, methodDesc, type, classLoader, methodNumber, proxyName));
    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods, boolean replaceable) {
        if (!Fakereplace.isRetransformationStarted()) {
            return false;
        }
        final Map<String, Set<Data>> knownFakeMethods = data.getManipulationData(loader);
        //methods that are known to need a rewrite to a generated static method
        final Map<Integer, Data> knownFakeMethodCallLocations = new HashMap<>();
        //methods that may need a rewrite to a generated static method
        final Map<Integer, AddedMethodInfo> potentialFakeMethodCallLocations = new HashMap<>();
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
                if (methodName.equals("<clinit>") || methodName.equals("<init>")) {
                    continue;
                }
                boolean handled = false;
                if (knownFakeMethods.containsKey(className)) {
                    for (Data data : knownFakeMethods.get(className)) {
                        if (methodName.equals(data.getMethodName()) && methodDesc.equals(data.getMethodDesc())) {
                            // store the location in the const pool of the method ref
                            knownFakeMethodCallLocations.put(i, data);
                            // we have found a method call
                            // now lets replace it
                            handled = true;
                            break;
                        }

                    }
                }
                if (loader != null && !handled && !className.equals(file.getName()) && replaceable) {
                    //may be an added method
                    //if the field does not actually exist yet we just assume it is about to come into existence
                    //and rewrite it anyway
                    BaseClassData data = ClassDataStore.instance().getBaseClassData(loader, className);
                    if (data != null) {
                        boolean noClassData = false;
                        MethodData method = null;
                        try {
                            Class<?> mainClass = loader.loadClass(className);
                            Set<Class> allClasses = new HashSet<>();
                            addToAllClasses(mainClass, allClasses);
                            for (Class clazz : allClasses) {
                                data = ClassDataStore.instance().getBaseClassData(clazz.getClassLoader(), clazz.getName());
                                if (data == null) {
                                    noClassData = true;
                                    break;
                                }
                                method = data.getMethodOrConstructor(methodName, methodDesc);
                                if (method != null) {
                                    break;
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            noClassData = true;
                        }
                        if (!noClassData) {
                            if (method == null) {
                                //this is a new method
                                //lets deal with it
                                int methodNo = MethodIdentifierStore.instance().getMethodNumber(methodName, methodDesc);
                                potentialFakeMethodCallLocations.put(i, new AddedMethodInfo(methodNo, className, methodName, methodDesc));
                            } else if (!Modifier.isPublic(method.getAccessFlags())) {
                                boolean requiresVisibilityUpgrade = false;
                                if (Modifier.isPrivate(method.getAccessFlags())) {
                                    requiresVisibilityUpgrade = true;
                                } else if (!Modifier.isProtected(method.getAccessFlags())) {
                                    //we can't handle protected properly, because we need to know the class heirachy
                                    //this is package local, so we check the package names
                                    boolean thisDefault = !file.getName().contains(".");
                                    boolean thatDefault = !className.contains(".");
                                    if (thisDefault && !thatDefault) {
                                        requiresVisibilityUpgrade = true;
                                    } else if (thatDefault && !thisDefault) {
                                        requiresVisibilityUpgrade = true;
                                    } else if (!thatDefault) {
                                        String thatPackage = className.substring(0, className.lastIndexOf("."));
                                        String thisPackage = file.getName().substring(0, file.getName().lastIndexOf("."));
                                        if (!thisPackage.equals(thatPackage)) {
                                            requiresVisibilityUpgrade = true;
                                        }
                                    }
                                }
                                if (requiresVisibilityUpgrade) {
                                    int methodNo = MethodIdentifierStore.instance().getMethodNumber(methodName, methodDesc);
                                    potentialFakeMethodCallLocations.put(i, new AddedMethodInfo(methodNo, className, methodName, methodDesc));
                                }
                            }
                        }
                    }
                }
            }
        }

        // this means we found an instance of the call, now we have to iterate
        // through the methods and replace instances of the call
        if (!knownFakeMethodCallLocations.isEmpty() || !potentialFakeMethodCallLocations.isEmpty()) {

            handleLambdas(file, knownFakeMethodCallLocations, pool);


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
                            if (potentialFakeMethodCallLocations.containsKey(val)) {
                                AddedMethodInfo methodInfo = potentialFakeMethodCallLocations.get(val);
                                Data data = new Data(methodInfo.className, methodInfo.name, methodInfo.desc, op == Opcode.INVOKESTATIC ? Type.STATIC : op == Opcode.INVOKEINTERFACE ? Type.INTERFACE : Type.VIRTUAL, loader, methodInfo.number, null);
                                handleFakeMethodCall(file, modifiedMethods, m, it, index, op, data);
                            } else if (knownFakeMethodCallLocations.containsKey(val)) {
                                Data data = knownFakeMethodCallLocations.get(val);
                                handleFakeMethodCall(file, modifiedMethods, m, it, index, op, data);

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

    private void handleLambdas(ClassFile file, Map<Integer, Data> knownFakeMethodCallLocations, ConstPool pool) {
        //check the bootstrapmethod's attribute
        //this makes lambda support work
        AttributeInfo bootstrapMethods = file.getAttribute(BootstrapMethodsAttribute.tag);
        if (bootstrapMethods instanceof BootstrapMethodsAttribute) {
            BootstrapMethodsAttribute boot = (BootstrapMethodsAttribute) bootstrapMethods;
            boolean replaceBootstrap = false;
            BootstrapMethodsAttribute.BootstrapMethod[] replacement = boot.getMethods();
            for (BootstrapMethodsAttribute.BootstrapMethod method : replacement) {

                //initial support for lambda replacement
                //first we look for all invocations on LambdaMetafactory
                int kind = pool.getMethodHandleKind(method.methodRef);
                if (kind == ConstPool.REF_invokeStatic) {
                    int nameAndType = pool.getMethodHandleIndex(method.methodRef);
                    String className = pool.getMethodrefClassName(nameAndType);
                    String methodName = pool.getMethodrefName(nameAndType);
                    if (className.equals(LambdaMetafactory.class.getName())) {
                        if (methodName.equals("metafactory")) {
                            //we have a lambda instance
                            //does it reference a new method
                            int methodHandleArg = method.arguments[1];
                            kind = pool.getMethodHandleKind(methodHandleArg);
                            if (kind == ConstPool.REF_invokeStatic || kind == ConstPool.REF_invokeVirtual || kind == ConstPool.REF_invokeSpecial) {
                                int methodRefArg = pool.getMethodHandleIndex(methodHandleArg);
                                if (knownFakeMethodCallLocations.containsKey(methodRefArg)) {
                                    //the lambda references a new method

                                    replaceBootstrap = true;
                                    Data target = knownFakeMethodCallLocations.get(methodRefArg);
                                    String type = pool.getMethodrefType(methodRefArg);
                                    String name = pool.getMethodrefName(methodRefArg);
                                    if (kind != ConstPool.REF_invokeStatic) {
                                        type = "(" + DescriptorUtils.extToInt(file.getName()) + type.substring(1);
                                    }

                                    int newMethodRef = pool.addMethodrefInfo(pool.addClassInfo(target.getProxyName()), name, type);
                                    int newMethodHandle = pool.addMethodHandleInfo(ConstPool.REF_invokeStatic, newMethodRef);
                                    method.arguments[1] = newMethodHandle;
                                }
                            }


                        }
                    }
                }
            }
            if (replaceBootstrap) {
                file.addAttribute(new BootstrapMethodsAttribute(file.getConstPool(), replacement));
            }
        }
    }

    private void addToAllClasses(Class<?> clazz, Set<Class> allClasses) {
        while (clazz != null) {
            allClasses.add(clazz);
            for (Class<?> iface : clazz.getInterfaces()) {
                addToAllClasses(iface, allClasses);
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void handleFakeMethodCall(ClassFile file, Set<MethodInfo> modifiedMethods, MethodInfo m, CodeIterator it, int index, int op, Data data) throws BadBytecode {
        //NOP out the whole thing
        it.writeByte(CodeIterator.NOP, index);
        it.writeByte(CodeIterator.NOP, index + 1);
        it.writeByte(CodeIterator.NOP, index + 2);
        if (op == CodeIterator.INVOKEINTERFACE) {
            // INVOKEINTERFACE has some extra parameters
            it.writeByte(CodeIterator.NOP, index + 3);
            it.writeByte(CodeIterator.NOP, index + 4);
        }
        //now we write some bytecode to invoke it directly
        final boolean staticMethod = data.getType() == Type.STATIC;
        Bytecode byteCode = new Bytecode(file.getConstPool());

        // stick the method number in the const pool then load it onto the
        // stack

        ManipulationUtils.pushParametersIntoArray(byteCode, data.getMethodDesc());
        int scind = file.getConstPool().addIntegerInfo(data.getMethodNumber());
        byteCode.addLdc(scind);
        byteCode.add(Opcode.SWAP);
        // invoke the added method
        if (staticMethod) {
            byteCode.addInvokestatic(data.getClassName(), Constants.ADDED_STATIC_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
        } else if (data.getType() == Type.INTERFACE) {
            byteCode.addInvokeinterface(data.getClassName(), Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;", 3);
        } else {
            byteCode.addInvokevirtual(data.getClassName(), Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
        }
        // cast it to the appropriate type and return it
        String returnType = DescriptorUtils.getReturnType(data.getMethodDesc());
        if (returnType.length() == 1 && !returnType.equals("V")) {
            Boxing.unbox(byteCode, returnType.charAt(0));
        } else if (returnType.equals("V")) {
            byteCode.add(Opcode.POP);
        } else {
            byteCode.addCheckcast(returnType.substring(1, returnType.length() - 1));
        }
        it.insertEx(byteCode.get());
        modifiedMethods.add(m);
    }

    public enum Type {
        VIRTUAL, STATIC, INTERFACE
    }


    private static class AddedMethodInfo {
        final int number;
        final String className;
        final String name;
        final String desc;

        private AddedMethodInfo(int number, String className, String name, String desc) {
            this.number = number;
            this.className = className;
            this.name = name;
            this.desc = desc;
        }
    }

    /**
     * @author Stuart Douglas
     */
    private static class Data implements ClassLoaderFiltered<Data> {
        private final String className;
        private final String methodName;
        private final String methodDesc;
        private final Type type;
        private final ClassLoader classLoader;
        private final int methodNumber;
        private final String proxyName;

        public Data(String className, String methodName, String methodDesc, Type type, ClassLoader classLoader, int methodNumber, String proxyName) {
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.type = type;
            this.classLoader = classLoader;
            this.methodNumber = methodNumber;
            this.proxyName = proxyName;
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

        public Type getType() {
            return type;
        }

        public int getMethodNumber() {
            return methodNumber;
        }

        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public String getProxyName() {
            return proxyName;
        }

        @Override
        public String toString() {
            return "FakeMethodCallData{" +
                    "clazz='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", newMethodDesc='" + methodDesc + '\'' +
                    ", type=" + type +
                    ", classLoader=" + classLoader +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data that = (Data) o;

            if (className != null ? !className.equals(that.className) : that.className != null) return false;
            if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
            if (methodDesc != null ? !methodDesc.equals(that.methodDesc) : that.methodDesc != null)
                return false;
            if (type != that.type) return false;
            return classLoader != null ? classLoader.equals(that.classLoader) : that.classLoader == null;

        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            result = 31 * result + (methodDesc != null ? methodDesc.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (classLoader != null ? classLoader.hashCode() : 0);
            return result;
        }

        public Data getInstance() {
            return this;
        }

    }
}
