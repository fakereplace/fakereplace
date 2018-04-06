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

package org.fakereplace.core;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.manip.ManipulationUtils;
import org.fakereplace.manip.Manipulator;
import org.fakereplace.reflection.ReflectionInstrumentationSetup;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.util.NoInstrument;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * This file is the transformer that instruments classes as they are added to
 * the system.
 *
 * @author stuart
 */
public class Transformer implements FakereplaceTransformer {

    private static final Manipulator manipulator = new Manipulator();


    Transformer() {
        ReflectionInstrumentationSetup.setup(manipulator);
    }

    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods, boolean replaceable) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        boolean modified = false;
        if (classBeingRedefined != null) {
            ClassDataStore.instance().markClassReplaced(classBeingRedefined);
        }
        // we do not instrument any classes from fakereplace
        // if we did we get an endless loop
        // we also avoid instrumenting much of the java/lang and
        // java/io namespace except for java/lang/reflect/Proxy
        if (BuiltinClassData.skipInstrumentation(className)) {
            if (classBeingRedefined != null && manipulator.transformClass(file, loader, false, modifiedMethods, replaceable)) {
                modified = true;
            }
            return modified;
        }


        if (classBeingRedefined == null) {
            AnnotationsAttribute at = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.invisibleTag);
            if (at != null) {
                // NoInstrument is used for testing or by integration modules
                Object an = at.getAnnotation(NoInstrument.class.getName());
                if (an != null) {
                    return modified;
                }
            }
        }

        if (manipulator.transformClass(file, loader, replaceable, modifiedMethods, replaceable)) {
            modified = true;
        }

        if (replaceable) {
            if ((AccessFlag.ENUM & file.getAccessFlags()) == 0 && (AccessFlag.ANNOTATION & file.getAccessFlags()) == 0) {
                modified = true;

                if (file.isInterface()) {
                    addAbstractMethodForInstrumentation(file);
                } else {
                    addMethodForInstrumentation(file);
                    addConstructorForInstrumentation(file);
                    addStaticConstructorForInstrumentation(file);
                }
            }
            if (classBeingRedefined == null) {
                BaseClassData baseData = new BaseClassData(file, loader, replaceable);
                ClassDataStore.instance().saveClassData(loader, baseData.getInternalName(), baseData);
            }
        }
        // SerialVersionUIDChecker.testReflectionInfo(loader, file.getName(),
        // file.getSuperclass(), classfileBuffer);
        return modified;
    }

    /**
     * Adds a method to a class that re can redefine when the class is reloaded
     */
    private void addMethodForInstrumentation(ClassFile file) {
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.SYNTHETIC);

            Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
            if (BuiltinClassData.skipInstrumentation(file.getSuperclass())) {
                b.addNew(NoSuchMethodError.class.getName());
                b.add(Opcode.DUP);
                b.addInvokespecial(NoSuchMethodError.class.getName(), "<init>", "()V");
                b.add(Opcode.ATHROW);
            } else {
                // delegate to the parent class
                b.add(Bytecode.ALOAD_0);
                b.add(Bytecode.ILOAD_1);
                b.add(Bytecode.ALOAD_2);
                b.addInvokespecial(file.getSuperclass(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
                b.add(Bytecode.ARETURN);

            }
            CodeAttribute ca = b.toCodeAttribute();
            m.setCodeAttribute(ca);
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_STATIC_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC | AccessFlag.SYNTHETIC);
            Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
            b.addNew(NoSuchMethodError.class.getName());
            b.add(Opcode.DUP);
            b.addInvokespecial(NoSuchMethodError.class.getName(), "<init>", "()V");
            b.add(Opcode.ATHROW);
            CodeAttribute ca = b.toCodeAttribute();
            m.setCodeAttribute(ca);
            file.addMethod(m);

        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
    }

    private static void addStaticConstructorForInstrumentation(ClassFile file) {
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), "<clinit>", "()V");
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
            Bytecode b = new Bytecode(file.getConstPool());
            b.add(Opcode.RETURN);
            m.setCodeAttribute(b.toCodeAttribute());
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Adds a method to a class that re can redefine when the class is reloaded
     */
    private void addAbstractMethodForInstrumentation(ClassFile file) {
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.ABSTRACT | AccessFlag.SYNTHETIC);
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
    }

    private void addConstructorForInstrumentation(ClassFile file) {

        MethodInfo ret = new MethodInfo(file.getConstPool(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
        Bytecode code = new Bytecode(file.getConstPool());
        // if the class does not have a constructor return
        if (!ManipulationUtils.addBogusConstructorCall(file, code)) {
            return;
        }
        CodeAttribute ca = code.toCodeAttribute();
        ca.setMaxLocals(4);
        ret.setCodeAttribute(ca);
        ret.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.SYNTHETIC);
        try {
            ca.computeMaxStack();
            file.addMethod(ret);
        } catch (DuplicateMemberException e) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Manipulator getManipulator() {
        return manipulator;
    }

}
