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

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import org.fakereplace.util.JumpMarker;
import org.fakereplace.util.JumpUtils;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

public class ClassLoaderInstrumentation {

    /**
     * This method instruments class loaders so that they can load our helper
     * classes.
     */
    @SuppressWarnings("unchecked")
    public static boolean redefineClassLoader(ClassFile classFile, Set<MethodInfo> modifiedMethods) throws BadBytecode {
        boolean modified = false;
        for (MethodInfo method : (List<MethodInfo>) classFile.getMethods()) {
            if (Modifier.isStatic(method.getAccessFlags())) {
                continue;
            }
            if (method.getName().equals("loadClass") && (method.getDescriptor().equals("(Ljava/lang/String;)Ljava/lang/Class;") || method.getDescriptor().equals("(Ljava/lang/String;Z)Ljava/lang/Class;"))) {
                modifiedMethods.add(method);
                modified = true;

                if (method.getCodeAttribute().getMaxLocals() < 4) {
                    method.getCodeAttribute().setMaxLocals(4);
                }
                // now we instrument the loadClass
                // if the system requests a class from the generated class package
                // then
                // we check to see if it is already loaded.
                // if not we try and get the class definition from GlobalData
                // we do not need to delegate as GlobalData will only
                // return the data to the correct classloader.
                // if the data is not null then we define the class, link
                // it if requested and return it.
                final CodeIterator iterator = method.getCodeAttribute().iterator();
                final Bytecode b = new Bytecode(classFile.getConstPool());
                b.addAload(1);
                b.addAload(0);
                b.addInvokestatic(ClassLookupManager.class.getName(), "getClassData", "(Ljava/lang/String;Ljava/lang/Object;)[B");
                b.add(Opcode.DUP);
                b.add(Opcode.IFNULL);
                JumpMarker jumpEnd = JumpUtils.addJumpInstruction(b);

                //now we need to do the findLoadedClasses thing
                b.addAload(0);
                b.addAload(1);
                b.addInvokevirtual("java.lang.ClassLoader", "findLoadedClass", "(Ljava/lang/String;)Ljava/lang/Class;");
                b.add(Opcode.DUP);
                b.add(Opcode.IFNULL);
                JumpMarker notFound = JumpUtils.addJumpInstruction(b);
                b.add(Opcode.ARETURN);
                notFound.mark();
                b.add(Opcode.POP);
                b.addAstore(3);
                b.addAload(0);
                b.addAload(1);
                b.addAload(3);
                b.addIconst(0);
                b.addAload(3);
                b.add(Opcode.ARRAYLENGTH);
                b.addInvokevirtual("java.lang.ClassLoader", "defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
                if (method.getDescriptor().equals("Ljava/lang/String;Z)Ljava/lang/Class;")) {
                    b.addIload(2);
                } else {
                    b.addIconst(0);
                }
                b.add(Opcode.IFEQ);
                final JumpMarker linkJumpEnd = JumpUtils.addJumpInstruction(b);
                b.add(Opcode.DUP);
                b.addAload(0);
                b.add(Opcode.SWAP);
                b.addInvokevirtual("java.lang.ClassLoader", "resolveClass", "(Ljava/lang/Class;)V");
                linkJumpEnd.mark();
                b.add(Opcode.ARETURN);
                jumpEnd.mark();
                b.add(Opcode.POP);

                if (!classFile.getName().startsWith("java.") && !classFile.getName().startsWith("com.sun") && !classFile.getName().startsWith("sun")&& !classFile.getName().startsWith("jdk.internal")) {
                    //now we need to check if this is a fakereplace class
                    //and if so always delegate to the appropriate loader
                    b.addAload(1);
                    b.addLdc("org.fakereplace");
                    b.addInvokevirtual(String.class.getName(), "startsWith", "(Ljava/lang/String;)Z");
                    b.add(Opcode.IFEQ);
                    JumpMarker notFakereplace = JumpUtils.addJumpInstruction(b);
                    //so this is a fakereplace class, delegate to the system loader
                    b.addInvokestatic(ClassLoader.class.getName(), "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
                    b.addAload(1);
                    b.addInvokevirtual(ClassLoader.class.getName(), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
                    b.add(Opcode.ARETURN);
                    notFakereplace.mark();
                }

                iterator.insert(b.get());
                method.getCodeAttribute().computeMaxStack();
            }
        }
        return modified;
    }


    private ClassLoaderInstrumentation() {

    }


}
