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

import org.fakereplace.replacement.notification.ChangedClassImpl;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * Transformer that clears the class cache, because reflection is now banned it requires some trickiness
 */
public class ClassInfoTransformer implements FakereplaceTransformer {

    public static volatile Runnable clearAction;

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods, boolean replaceable) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if (className.equals("com/sun/beans/introspect/ClassInfo")) {
            file.addInterface(Runnable.class.getName());
            MethodInfo run = new MethodInfo(file.getConstPool(), "run", "()V");
            run.setAccessFlags(AccessFlag.PUBLIC);
            Bytecode b = new Bytecode(file.getConstPool(), 1, 1);
            b.addGetstatic(file.getName(), "CACHE", "Lcom/sun/beans/util/Cache;");
            b.addInvokevirtual("com/sun/beans/util/Cache", "clear", "()V");
            b.add(Opcode.RETURN);
            run.setCodeAttribute(b.toCodeAttribute());
            file.addMethod(run);

            MethodInfo m  = file.getMethod("<init>");
            CodeIterator iterator = m.getCodeAttribute().iterator();
            int pos = 0;
            while (iterator.hasNext()) {
                pos = iterator.next();
            }
            b = new Bytecode(file.getConstPool(), 1, 0);
            b.add(Bytecode.ALOAD_0);
            b.addPutstatic(ClassInfoTransformer.class.getName(), "clearAction", "Ljava/lang/Runnable;");
            iterator.insert(pos, b.get());
            m.getCodeAttribute().computeMaxStack();
            return true;
        }
        return false;
    }

    static void clearClassInfoCache() {
        if (clearAction != null) {
            clearAction.run();
        }
    }
}
