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

package org.fakereplace.replacement;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.SignatureAttribute;
import org.fakereplace.classloading.ProxyDefinitionStore;
import org.fakereplace.core.BuiltinClassData;
import org.fakereplace.core.Constants;
import org.fakereplace.core.Transformer;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.data.FakeMethodCallData;
import org.fakereplace.manip.util.Boxing;
import org.fakereplace.manip.util.ManipulationUtils;
import org.fakereplace.manip.util.ManipulationUtils.MethodReturnRewriter;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.runtime.MethodIdentifierStore;
import org.fakereplace.transformation.FakereplaceTransformer;
import org.fakereplace.util.AccessFlagUtils;
import org.fakereplace.util.DescriptorUtils;

public class MethodReplacementTransformer implements FakereplaceTransformer {

    private static final Logger logger = Logger.getLogger(MethodReplacementTransformer.class);

    private static String generateProxyInvocationBytecode(MethodInfo mInfo, int methodNumber, String className, ClassLoader loader, boolean staticMethod, boolean isInterface) throws BadBytecode {
        String proxyName = ProxyDefinitionStore.getProxyName();
        ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
        proxy.setVersionToJava5();
        proxy.setAccessFlags(AccessFlag.PUBLIC);

        // now generate our proxy that is used to actually call the method
        // we use a proxy because it makes the re-writing of loaded classes
        // much simpler

        MethodInfo nInfo;
        if (staticMethod) {
            nInfo = new MethodInfo(proxy.getConstPool(), mInfo.getName(), mInfo.getDescriptor());
        } else {
            // the descriptor is different as now there is an extra parameter for a
            // static call
            String nDesc = "(" + DescriptorUtils.extToInt(className) + mInfo.getDescriptor().substring(1);
            nInfo = new MethodInfo(proxy.getConstPool(), mInfo.getName(), nDesc);
        }
        copyMethodAttributes(mInfo, nInfo);

        // set the sync bit on the proxy if it was set on the method

        nInfo.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
        Bytecode proxyBytecode = new Bytecode(proxy.getConstPool());

        int paramOffset = 0;
        // if this is not a static method then we need to load the instance
        // onto the stack
        if (!staticMethod) {
            proxyBytecode.addAload(0);
            paramOffset = 1;
        }

        // stick the method number in the const pool then load it onto the
        // stack
        int scind = proxy.getConstPool().addIntegerInfo(methodNumber);
        proxyBytecode.addLdc(scind);

        String[] types = DescriptorUtils.descriptorStringToParameterArray(mInfo.getDescriptor());
        // create a new array the same size as the parameter array
        int index = proxyBytecode.getConstPool().addIntegerInfo(types.length);
        proxyBytecode.addLdc(index);
        // create new array to use to pass our parameters
        proxyBytecode.addAnewarray("java.lang.Object");
        int locals = types.length + paramOffset;
        for (int i = 0; i < types.length; ++i) {
            // duplicate the array reference on the stack
            proxyBytecode.add(Opcode.DUP);
            // load the array index into the stack
            index = proxyBytecode.getConstPool().addIntegerInfo(i);
            proxyBytecode.addLdc(index);

            char tp = types[i].charAt(0);
            if (tp != 'L' && tp != '[') {
                // we have a primitive type
                switch (tp) {
                    case 'J':
                        proxyBytecode.addLload(i + paramOffset);
                        locals++;
                        break;
                    case 'D':
                        proxyBytecode.addDload(i + paramOffset);
                        locals++;
                        break;
                    case 'F':
                        proxyBytecode.addFload(i + paramOffset);
                        break;
                    default:
                        proxyBytecode.addIload(i + paramOffset);
                }
                // lets box it
                Boxing.box(proxyBytecode, tp);
            } else {
                proxyBytecode.addAload(i + paramOffset); // load parameter i onto
                // the stack
            }
            proxyBytecode.add(Opcode.AASTORE);// store the value in the array

        }

        // invoke the added static method
        if (staticMethod) {
            proxyBytecode.addInvokestatic(className, Constants.ADDED_STATIC_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
        } else if (isInterface) {
            proxyBytecode.addInvokeinterface(className, Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;", 3);
        } else {
            proxyBytecode.addInvokevirtual(className, Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
        }
        // cast it to the appropriate type and return it
        ManipulationUtils.MethodReturnRewriter.addReturnProxyMethod(mInfo.getDescriptor(), proxyBytecode);
        CodeAttribute ca = proxyBytecode.toCodeAttribute();
        ca.setMaxLocals(locals);

        ca.computeMaxStack();
        nInfo.setCodeAttribute(ca);

        // now we have the static method that actually does the re-writes.
        // if this is a virtual method then we need to add another virtual method
        // with the exact signature of the existing
        // method.
        // this is so that we do not need to instrument the reflection API to much
        if (!staticMethod) {
            // as this method is never called the bytecode just returns
            MethodInfo method = new MethodInfo(proxy.getConstPool(), mInfo.getName(), mInfo.getDescriptor());
            method.setAccessFlags(mInfo.getAccessFlags());
            if ((method.getAccessFlags() & AccessFlag.ABSTRACT) == 0) {
                Bytecode b = new Bytecode(proxy.getConstPool());
                String ret = DescriptorUtils.getReturnType(mInfo.getDescriptor());
                if (ret.length() == 1) {
                    if (ret.equals("V")) {
                        b.add(Opcode.RETURN);
                    } else if (ret.equals("D")) {
                        b.add(Opcode.DCONST_0);
                        b.add(Opcode.DRETURN);
                    } else if (ret.equals("F")) {
                        b.add(Opcode.FCONST_0);
                        b.add(Opcode.FRETURN);
                    } else if (ret.equals("J")) {
                        b.add(Opcode.LCONST_0);
                        b.add(Opcode.LRETURN);
                    } else {
                        b.add(Opcode.ICONST_0);
                        b.add(Opcode.IRETURN);
                    }
                } else {
                    b.add(Opcode.ACONST_NULL);
                    b.add(Opcode.ARETURN);
                }
                method.setCodeAttribute(b.toCodeAttribute());
                method.getCodeAttribute().computeMaxStack();
                method.getCodeAttribute().setMaxLocals(locals);
            }

            copyMethodAttributes(mInfo, method);
            try {
                proxy.addMethod(method);
            } catch (DuplicateMemberException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            proxy.addMethod(nInfo);
        } catch (DuplicateMemberException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bytes);
            proxy.write(dos);
            ProxyDefinitionStore.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
            return proxyName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Adds a method to a class
     */
    private static Class<?> addMethod(ClassFile file, ClassLoader loader, MethodInfo mInfo, Set<FakeMethod> builder, CodeAttribute bytecode, boolean staticMethod, Class oldClass) {
        int methodCount = MethodIdentifierStore.instance().getMethodNumber(mInfo.getName(), mInfo.getDescriptor());
        try {
            if ((AccessFlag.ABSTRACT & mInfo.getAccessFlags()) == 0) {
                // abstract methods don't get a body
                generateBoxedConditionalCodeBlock(methodCount, mInfo, file.getConstPool(), bytecode, staticMethod, false);
            }
            String proxyName = generateProxyInvocationBytecode(mInfo, methodCount, file.getName(), loader, staticMethod, file.isInterface());
            ClassDataStore.instance().registerProxyName(oldClass, proxyName);
            Transformer.getManipulator().addFakeMethodCallRewrite(new FakeMethodCallData(file.getName(), mInfo.getName(), mInfo.getDescriptor(), staticMethod ? FakeMethodCallData.Type.STATIC : file.isInterface() ? FakeMethodCallData.Type.INTERFACE : FakeMethodCallData.Type.VIRTUAL, loader, methodCount));

            builder.add(new FakeMethod(mInfo.getName(), proxyName, mInfo.getDescriptor(), mInfo.getAccessFlags()));
            if (!staticMethod) {
                Class<?> sup = oldClass.getSuperclass();
                while (sup != null && !sup.getName().equals(Object.class.getName())) {
                    for (Method m : sup.getDeclaredMethods()) {
                        if (m.getName().equals(mInfo.getName())) {
                            if (DescriptorUtils.getDescriptor(m).equals(mInfo.getDescriptor())) {
                                Transformer.getManipulator().rewriteSubclassCalls(file.getName(), loader, sup.getName(), sup.getClassLoader(), mInfo.getName(), mInfo.getDescriptor());
                                return sup;
                            }
                        }
                    }
                    sup = sup.getSuperclass();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will take a method body and add it to an added method local
     * the bytecode is inserted inside a conditional that will only run the code
     * if the method number is correct variables are removed from the parameter
     * array and unboxed if nessesary the return value is boxed if nessesary
     * <p>
     * Much of this work is handled by helper classes
     */
    private static void generateBoxedConditionalCodeBlock(int methodNumber, MethodInfo mInfo, ConstPool methodConstPool, CodeAttribute addedMethod, boolean staticMethod, boolean constructor) throws BadBytecode {

        // we need to insert a conditional
        Bytecode bc = new Bytecode(mInfo.getConstPool());
        CodeAttribute ca = (CodeAttribute) mInfo.getCodeAttribute().copy(mInfo.getConstPool(), Collections.emptyMap());
        if (staticMethod) {
            bc.addOpcode(Opcode.ILOAD_0);
        } else {
            bc.addOpcode(Opcode.ILOAD_1);
        }
        int methodCountIndex = methodConstPool.addIntegerInfo(methodNumber);
        bc.addLdc(methodCountIndex);
        bc.addOpcode(Opcode.IF_ICMPNE);

        // now we need to fix local variables and unbox parameters etc
        int addedCodeLength = mangleParameters(staticMethod, constructor, ca, mInfo.getDescriptor());
        int newMax = ca.getMaxLocals() + 2;
        if (constructor) {
            // for the extra
            newMax++;
        }
        if (newMax > addedMethod.getMaxLocals()) {
            addedMethod.setMaxLocals(newMax);
        }
        // later
        int offset = ca.getCodeLength();
        // offset is +3, 2 for the branch offset after the IF_ICMPNE and 1 to
        // take it past the end of the code
        ManipulationUtils.add16bit(bc, offset + 3); // add the branch offset

        // now we need to insert our generated conditional at the start of the
        // new method
        CodeIterator newInfo = ca.iterator();
        newInfo.insert(bc.get());
        // now insert the new method code at the beginning of the static method
        // code attribute
        addedMethod.iterator().insert(ca.getCode());

        // update the exception table

        int exOffset = bc.length() + addedCodeLength;
        for (int i = 0; i < mInfo.getCodeAttribute().getExceptionTable().size(); ++i) {
            int start = mInfo.getCodeAttribute().getExceptionTable().startPc(i) + exOffset;
            int end = mInfo.getCodeAttribute().getExceptionTable().endPc(i) + exOffset;
            int handler = mInfo.getCodeAttribute().getExceptionTable().handlerPc(i) + exOffset;
            int type = mInfo.getCodeAttribute().getExceptionTable().catchType(i);
            addedMethod.getExceptionTable().add(start, end, handler, type);
        }

        // now we need to make sure the function is returning an object
        // rewriteFakeMethod makes sure that the return type is properly boxed
        if (!constructor) {
            MethodReturnRewriter.rewriteFakeMethod(addedMethod.iterator(), mInfo.getDescriptor());
        }

    }

    private static MethodInfo createRemovedMethod(ClassFile file, MethodData md, Class<?> oldClass, Set<MethodData> methodsToRemove) {
        if (md.getMethodName().equals("<clinit>")) {
            return null; // if the static constructor is removed it gets added later on
            // in the process
        }

        // load up the existing method object

        MethodInfo m = new MethodInfo(file.getConstPool(), md.getMethodName(), md.getDescriptor());
        m.setAccessFlags(md.getAccessFlags());

        // put the old annotations on the class
        if (md.getMethodName().equals("<init>")) {
            Constructor<?> meth;
            try {
                meth = md.getConstructor(oldClass);
            } catch (Exception e) {
                throw new RuntimeException("Error accessing existing constructor via reflection in not found", e);
            }
            m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), meth));
        } else {
            Method meth;
            try {
                meth = md.getMethod(oldClass);
            } catch (Exception e) {
                throw new RuntimeException("Error accessing existing method via reflection in not found", e);
            }
            m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), meth));
        }
        Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
        b.addNew("java.lang.NoSuchMethodError");
        b.add(Opcode.DUP);
        b.addInvokespecial("java.lang.NoSuchMethodError", "<init>", "()V");
        b.add(Bytecode.ATHROW);
        CodeAttribute ca = b.toCodeAttribute();
        m.setCodeAttribute(ca);

        try {
            ca.computeMaxStack();
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            logger.error("Duplicate error", e);
        } catch (BadBytecode e) {
            logger.error("Bad bytecode", e);
        }
        methodsToRemove.add(md);
        return m;
    }

    private static void addConstructor(ClassFile file, ClassLoader loader, MethodInfo mInfo, Set<FakeMethod> builder, CodeAttribute bytecode, Class<?> oldClass) {
        int methodCount = MethodIdentifierStore.instance().getMethodNumber(mInfo.getName(), mInfo.getDescriptor());

        try {
            generateBoxedConditionalCodeBlock(methodCount, mInfo, file.getConstPool(), bytecode, false, true);
            String proxyName = generateFakeConstructorBytecode(mInfo, loader);
            ClassDataStore.instance().registerProxyName(oldClass, proxyName);
            Transformer.getManipulator().rewriteConstructorAccess(file.getName(), mInfo.getDescriptor(), methodCount, loader);
            builder.add(new FakeMethod(mInfo.getName(), proxyName, mInfo.getDescriptor(), mInfo.getAccessFlags(), methodCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * creates a class with a fake constructor that can be used by the reflection
     * api
     * <p>
     * Constructors are not invoked through the proxy class, instead we have to
     * do a lot more bytecode re-writing at the actual invocation sites
     */
    private static String generateFakeConstructorBytecode(MethodInfo mInfo, ClassLoader loader) throws BadBytecode {
        String proxyName = ProxyDefinitionStore.getProxyName();
        ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
        proxy.setVersionToJava5();
        proxy.setAccessFlags(AccessFlag.PUBLIC);

        // add our new annotations directly onto the new proxy method. This way
        // they will just work without registering them with the
        // AnnotationDataStore

        String[] types = DescriptorUtils.descriptorStringToParameterArray(mInfo.getDescriptor());
        // as this method is never called the bytecode just returns
        Bytecode b = new Bytecode(proxy.getConstPool());
        b.add(Opcode.ALOAD_0);
        b.addInvokespecial("java.lang.Object", "<init>", "()V");
        b.add(Opcode.RETURN);
        MethodInfo method = new MethodInfo(proxy.getConstPool(), mInfo.getName(), mInfo.getDescriptor());
        method.setAccessFlags(mInfo.getAccessFlags());
        method.setCodeAttribute(b.toCodeAttribute());
        method.getCodeAttribute().computeMaxStack();
        method.getCodeAttribute().setMaxLocals(types.length + 1);

        copyMethodAttributes(mInfo, method);

        try {
            proxy.addMethod(method);
        } catch (DuplicateMemberException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bytes);
            proxy.write(dos);
            ProxyDefinitionStore.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
            return proxyName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void copyMethodAttributes(MethodInfo oldMethod, MethodInfo newMethod) {
        AnnotationsAttribute annotations = (AnnotationsAttribute) oldMethod.getAttribute(AnnotationsAttribute.visibleTag);
        ParameterAnnotationsAttribute pannotations = (ParameterAnnotationsAttribute) oldMethod.getAttribute(ParameterAnnotationsAttribute.visibleTag);
        ExceptionsAttribute exAt = (ExceptionsAttribute) oldMethod.getAttribute(ExceptionsAttribute.tag);
        SignatureAttribute sigAt = (SignatureAttribute) oldMethod.getAttribute(SignatureAttribute.tag);
        if (annotations != null) {
            AttributeInfo newAnnotations = annotations.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
            newMethod.addAttribute(newAnnotations);
        }
        if (pannotations != null) {
            AttributeInfo newAnnotations = pannotations.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
            newMethod.addAttribute(newAnnotations);
        }
        if (sigAt != null) {
            AttributeInfo newAnnotations = sigAt.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
            newMethod.addAttribute(newAnnotations);
        }
        if (exAt != null) {
            AttributeInfo newAnnotations = exAt.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
            newMethod.addAttribute(newAnnotations);
        }
    }

    /**
     * Takes method parameters out of an array and puts them into local variables in the correct location. Also
     * deals with unboxing if necessary
     *
     * @return the length of the added code
     */
    private static int mangleParameters(boolean staticMethod, boolean constructor, CodeAttribute attribute, String methodSigniture) {
        try {
            int offset = 0;
            String[] data = DescriptorUtils.descriptorStringToParameterArray(methodSigniture);
            if (!staticMethod) {
                // non static methods have a this pointer as the first argument
                // which should not be mangled
                offset = 1;
            }

            // insert two new local variables, these are the fake method parameters
            attribute.insertLocalVar(offset, 1);
            attribute.insertLocalVar(offset, 1);
            if (constructor) {
                // constructors have an extra one
                attribute.insertLocalVar(offset, 1);
            }
            Bytecode code = new Bytecode(attribute.getConstPool());
            int varpos = offset + 2;
            if (constructor) {
                varpos++;
            }
            for (int i = 0; i < data.length; ++i) {
                // push the parameter array onto the stack
                if (staticMethod) {
                    code.add(Opcode.ALOAD_1);
                } else {
                    code.add(Opcode.ALOAD_2);
                }
                int index = attribute.getConstPool().addIntegerInfo(i);
                code.addLdc(index);
                code.add(Opcode.AALOAD);
                // now we have the parameter on the stack.
                // what happens next depends on the type
                switch (data[i].charAt(0)) {
                    case 'L':
                        // add a checkcast substring is to get rid of the L
                        code.addCheckcast(data[i].substring(1));
                        // now stick it into its proper local variable
                        code.addAstore(varpos);
                        varpos++;
                        break;
                    case '[':
                        code.addCheckcast(data[i]);
                        // now stick it into its proper local variable
                        code.addAstore(varpos);
                        varpos++;
                        break;
                    case 'I':
                        // integer, we need to unbox it
                        Boxing.unboxInt(code);
                        code.addIstore(varpos);
                        varpos++;
                        break;
                    case 'S':
                        // short, we need to unbox it
                        Boxing.unboxShort(code);
                        code.addIstore(varpos);
                        varpos++;
                        break;
                    case 'B':
                        // short, we need to unbox it
                        Boxing.unboxByte(code);
                        code.addIstore(varpos);
                        varpos++;
                        break;
                    case 'J':
                        // long, we need to unbox it
                        Boxing.unboxLong(code);
                        code.addLstore(varpos);
                        varpos = varpos + 2;
                        break;
                    case 'F':
                        Boxing.unboxFloat(code);
                        code.addFstore(varpos);
                        varpos++;
                        break;
                    case 'D':
                        Boxing.unboxDouble(code);
                        code.addDstore(varpos);
                        varpos++;
                        break;
                    case 'C':
                        Boxing.unboxChar(code);
                        code.addIstore(varpos);
                        varpos++;
                        break;
                    case 'Z':
                        Boxing.unboxBoolean(code);
                        code.addIstore(varpos);
                        varpos++;
                        break;
                }

            }
            attribute.iterator().insert(0, code.get());
            return code.length();
        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        }

    }

    private CodeAttribute injectAddedStaticMethod(ClassFile file, Set<MethodInfo> modifiedMethods) throws DuplicateMemberException {
        MethodInfo addedStaticMethod = new MethodInfo(file.getConstPool(), Constants.ADDED_STATIC_METHOD_NAME, Constants.ADDED_STATIC_METHOD_DESCRIPTOR);
        modifiedMethods.add(addedStaticMethod);
        addedStaticMethod.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC | AccessFlag.SYNTHETIC);
        Bytecode staticMethodBytecode = new Bytecode(file.getConstPool(), 0, 3);
        staticMethodBytecode.addNew(NoSuchMethodError.class.getName());
        staticMethodBytecode.add(Opcode.DUP);
        staticMethodBytecode.addInvokespecial(NoSuchMethodError.class.getName(), "<init>", "()V");
        staticMethodBytecode.add(Opcode.ATHROW);
        CodeAttribute codeAttribute = staticMethodBytecode.toCodeAttribute();
        addedStaticMethod.setCodeAttribute(codeAttribute);
        file.addMethod(addedStaticMethod);

        return codeAttribute;
    }

    private CodeAttribute injectAddedConstructor(ClassFile file, Set<MethodInfo> modifiedMethods) throws DuplicateMemberException {
        MethodInfo addedConstructor = new MethodInfo(file.getConstPool(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
        modifiedMethods.add(addedConstructor);
        addedConstructor.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.SYNTHETIC);
        Bytecode constructorBytecode = new Bytecode(file.getConstPool(), 0, 4);

        final CodeAttribute constructorCodeAttribute;
        if (ManipulationUtils.addBogusConstructorCall(file, constructorBytecode)) {
            constructorCodeAttribute = constructorBytecode.toCodeAttribute();
            addedConstructor.setCodeAttribute(constructorCodeAttribute);
            constructorCodeAttribute.setMaxLocals(6);
            file.addMethod(addedConstructor);
        } else {
            constructorCodeAttribute = null;
        }
        return constructorCodeAttribute;
    }

    private void handleConstructor(MethodData oldMethod, MethodInfo newMethod, ClassFile file, Class<?> oldClass) {
        try {
            Constructor<?> constructor = oldMethod.getConstructor(oldClass);
            AnnotationDataStore.recordConstructorAnnotations(constructor, (AnnotationsAttribute) newMethod.getAttribute(AnnotationsAttribute.visibleTag));
            AnnotationDataStore.recordConstructorParameterAnnotations(constructor, (ParameterAnnotationsAttribute) newMethod.getAttribute(ParameterAnnotationsAttribute.visibleTag));
            // now revert the annotations:
            newMethod.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), constructor));
            newMethod.addAttribute(AnnotationReplacer.duplicateParameterAnnotationsAttribute(file.getConstPool(), constructor));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMethod(MethodData oldMethod, MethodInfo newMethod, ClassFile file, Class<?> oldClass) {
        // other methods
        // static constructors cannot have annotations so
        // we do not have to worry about them
        try {
            Method method = oldMethod.getMethod(oldClass);
            AnnotationDataStore.recordMethodAnnotations(method, (AnnotationsAttribute) newMethod.getAttribute(AnnotationsAttribute.visibleTag));
            AnnotationDataStore.recordMethodParameterAnnotations(method, (ParameterAnnotationsAttribute) newMethod.getAttribute(ParameterAnnotationsAttribute.visibleTag));
            // now revert the annotations:
            newMethod.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), method));
            newMethod.addAttribute(AnnotationReplacer.duplicateParameterAnnotationsAttribute(file.getConstPool(), method));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMethodAndConstructor(MethodData oldMethod, MethodInfo newMethod, ClassFile file, Class<?> oldClass) {
        // if it is the constructor
        if (newMethod.getName().equals("<init>")) {
            handleConstructor(oldMethod, newMethod, file, oldClass);
        } else if (!newMethod.getName().equals("<clinit>")) {
            handleMethod(oldMethod, newMethod, file, oldClass);
        }
    }

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> oldClass, ProtectionDomain protectionDomain,
                             ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods)
            throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if (oldClass == null || className == null) {
            return false;
        }
        final Set<MethodData> methodsToRemove = new HashSet<>();
        final Set<FakeMethod> methodsToAdd = new HashSet<>();
        final Set<FakeMethod> constructorsToAdd = new HashSet<>();
        BaseClassData data = ClassDataStore.instance().getBaseClassData(loader, className);
        // state for added static methods
        CodeAttribute staticCodeAttribute = null, virtualCodeAttribute = null, constructorCodeAttribute = null;
        try {
            // stick our added methods into the class file
            // we can't finalise the code yet because we will probably need
            // the add stuff to them
            MethodInfo addedMethod = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
            modifiedMethods.add(addedMethod);
            addedMethod.setAccessFlags(AccessFlag.PUBLIC);
            if (file.isInterface()) {
                addedMethod.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.ABSTRACT | AccessFlag.SYNTHETIC);
            } else {
                addedMethod.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.SYNTHETIC);
                Bytecode bytecode = new Bytecode(file.getConstPool(), 0, 3);
                if (BuiltinClassData.skipInstrumentation(file.getSuperclass())) {
                    bytecode.addNew(NoSuchMethodError.class.getName());
                    bytecode.add(Opcode.DUP);
                    bytecode.addInvokespecial(NoSuchMethodError.class.getName(), "<init>", "()V");
                    bytecode.add(Opcode.ATHROW);
                } else {
                    bytecode.add(Bytecode.ALOAD_0);
                    bytecode.add(Bytecode.ILOAD_1);
                    bytecode.add(Bytecode.ALOAD_2);
                    bytecode.addInvokespecial(file.getSuperclass(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
                    bytecode.add(Bytecode.ARETURN);
                }
                virtualCodeAttribute = bytecode.toCodeAttribute();
                addedMethod.setCodeAttribute(virtualCodeAttribute);

                staticCodeAttribute = injectAddedStaticMethod(file, modifiedMethods);
                constructorCodeAttribute = injectAddedConstructor(file, modifiedMethods);
            }
            file.addMethod(addedMethod);
        } catch (DuplicateMemberException e) {
            e.printStackTrace();
        }

        Set<MethodData> oldMethods = new HashSet<>();
        oldMethods.addAll(data.getMethods());

        ListIterator<?> newMethodsIterator = file.getMethods().listIterator();

        // now we iterate through all methods and constructors and compare new
        // and old. in the process we modify the new class so that is's signature
        // is exactly compatible with the old class, otherwise an
        // IncompatibleClassChange exception will be thrown
        while (newMethodsIterator.hasNext()) {
            MethodInfo newMethod = (MethodInfo) newMethodsIterator.next();
            MethodData md = null;
            boolean upgradedVisibility = false;
            for (MethodData oldMethod : oldMethods) {
                if (oldMethod.getMethodName().equals(newMethod.getName()) && oldMethod.getDescriptor().equals(newMethod.getDescriptor())) {

                    // if the access flags do not match then what we need to do
                    // depends on what has changed
                    if (oldMethod.getAccessFlags() != newMethod.getAccessFlags()) {
                        if (AccessFlagUtils.upgradeVisibility(newMethod.getAccessFlags(), oldMethod.getAccessFlags())) {
                            upgradedVisibility = true;
                        } else if (AccessFlagUtils.downgradeVisibility(newMethod.getAccessFlags(), oldMethod.getAccessFlags())) {
                            // ignore this, we don't need to do anything
                        } else {
                            // we can't handle this yet
                            continue;
                        }
                    }
                    newMethod.setAccessFlags(oldMethod.getAccessFlags());

                    handleMethodAndConstructor(oldMethod, newMethod, file, oldClass);

                    md = oldMethod;
                    break;
                }
            }
            // we do not need to deal with these
            if (newMethod.getName().equals(Constants.ADDED_METHOD_NAME) || newMethod.getName().equals(Constants.ADDED_STATIC_METHOD_NAME)) {
                break;
            }
            // This is a newly added method.
            // or the visilbility has been upgraded
            // with the visiblity upgrade we just copy the method
            // so it is still in the original
            if (md == null || upgradedVisibility) {
                if ((newMethod.getAccessFlags() & AccessFlag.STATIC) != 0) {
                    Class<?> c = addMethod(file, loader, newMethod, methodsToAdd, staticCodeAttribute, true, oldClass);
                    if (c != null) {
                        classesToRetransform.add(c);
                    }
                } else if ((newMethod.getName().equals("<init>"))) {
                    addConstructor(file, loader, newMethod, constructorsToAdd, constructorCodeAttribute, oldClass);
                } else if (newMethod.getName().equals("<clinit>")) {
                    // nop, we can't change this, just ignore it
                } else {
                    Class<?> c = addMethod(file, loader, newMethod, methodsToAdd, virtualCodeAttribute, false, oldClass);
                    if (c != null) {
                        classesToRetransform.add(c);
                    }
                }
                if (!upgradedVisibility) {
                    newMethodsIterator.remove();
                }
            } else {
                oldMethods.remove(md);
            }
            if (upgradedVisibility) {
                oldMethods.remove(md);
            }
        }
        // these methods have been removed, change them to throw a
        // MethodNotFoundError

        for (MethodData md : oldMethods) {
            if (md.getType() == MemberType.NORMAL) {
                MethodInfo removedMethod = createRemovedMethod(file, md, oldClass, methodsToRemove);
                if (removedMethod != null) {
                    modifiedMethods.add(removedMethod);
                }
            }
        }

        ClassDataStore.instance().modifyCurrentData(loader, className, builder -> {
            for (MethodData method : methodsToRemove) {
                builder.removeMethod(method);
            }
            for (FakeMethod fake : methodsToAdd) {
                ClassDataStore.instance().registerReplacedMethod(fake.proxyName,
                        builder.addFakeMethod(fake.name, fake.descriptor, fake.proxyName, fake.accessFlags));
            }
            for (FakeMethod fake : constructorsToAdd) {
                ClassDataStore.instance().registerReplacedMethod(fake.proxyName,
                        builder.addFakeConstructor(fake.name, fake.descriptor, fake.proxyName, fake.accessFlags, fake.methodCount));
            }

        });

        // if we did not return from a virtual method we need to call the parent
        // method directly so to this end we append some stuff to the bottom of
        // the method declaration to propagate the call to the parent
        if (!file.isInterface()) {
            try {
                staticCodeAttribute.computeMaxStack();
                virtualCodeAttribute.computeMaxStack();
                if (constructorCodeAttribute != null) {
                    constructorCodeAttribute.computeMaxStack();
                }
            } catch (BadBytecode e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static final class FakeMethod {
        final String name;
        final String proxyName;
        final String descriptor;
        final int accessFlags;
        final int methodCount;

        private FakeMethod(String name, String proxyName, String descriptor, int accessFlags) {
            this.name = name;
            this.proxyName = proxyName;
            this.descriptor = descriptor;
            this.accessFlags = accessFlags;
            this.methodCount = -1;
        }

        private FakeMethod(String name, String proxyName, String descriptor, int accessFlags, int methodCount) {
            this.name = name;
            this.proxyName = proxyName;
            this.descriptor = descriptor;
            this.accessFlags = accessFlags;
            this.methodCount = methodCount;
        }
    }
}
