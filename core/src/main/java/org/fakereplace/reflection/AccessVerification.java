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

package org.fakereplace.reflection;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import org.fakereplace.core.ProxyDefinitionStore;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * @author Stuart Douglas
 */
class AccessVerification {

    /**
     * The way to do this has changed between JDK8 and JDK9.
     * <p>
     * To make it compile on both we generate an accessor at runtime using javassist
     */
    private static final Function<Integer, Class<?>> GET_CALLER;

    static {

        Function<Integer, Class<?>> caller = null;
        try {
            Class.forName("sun.reflect.Reflection");
            //JDK8
            String classname = ProxyDefinitionStore.getProxyName();
            ClassFile cf = new ClassFile(false, classname, null);
            cf.setAccessFlags(AccessFlag.PUBLIC);
            cf.setInterfaces(new String[]{Function.class.getName()});
            MethodInfo m = new MethodInfo(cf.getConstPool(), "<init>", "()V");
            m.setAccessFlags(AccessFlag.PUBLIC);
            Bytecode b = new Bytecode(m.getConstPool(), 1, 1);
            b.addAload(0);
            b.addInvokespecial(Object.class.getName(), "<init>", "()V");
            b.add(Opcode.RETURN);
            m.setCodeAttribute(b.toCodeAttribute());
            m.getCodeAttribute().computeMaxStack();
            cf.addMethod(m);

            m = new MethodInfo(cf.getConstPool(), "apply", "(Ljava/lang/Object;)Ljava/lang/Object;");
            m.setAccessFlags(AccessFlag.PUBLIC);
            b = new Bytecode(m.getConstPool(), 2, 2);
            b.addAload(1);
            b.addCheckcast(Integer.class.getName());
            b.addInvokevirtual(Integer.class.getName(), "intValue", "()I");
            b.addIconst(1);
            b.add(Opcode.IADD);
            b.addInvokestatic("sun.reflect.Reflection", "getCallerClass", "(I)Ljava/lang/Class;");
            b.add(Opcode.ARETURN);
            m.setCodeAttribute(b.toCodeAttribute());
            m.getCodeAttribute().computeMaxStack();
            cf.addMethod(m);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bytes);
            cf.write(dos);
            ProxyDefinitionStore.saveProxyDefinition(ClassLoader.getSystemClassLoader(), classname, bytes.toByteArray());
            Class<?> clazz = Class.forName(classname, true, ClassLoader.getSystemClassLoader());
            caller = (Function<Integer, Class<?>>) clazz.newInstance();
        } catch (ClassNotFoundException | DuplicateMemberException | BadBytecode e) {
            e.printStackTrace();
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e); //should never happen
        }
        GET_CALLER = caller;
    }

    static void ensureMemberAccess(Class<?> caller, Class<?> declaring, int modifiers) throws IllegalAccessException {
        if (caller != null && declaring != null) {
            if (!verifyMemberAccess(caller, declaring, modifiers)) {
                throw new IllegalAccessException("Class " + caller.getName() + " can not access a member of class " + declaring.getName() + " with modifiers \"" + Modifier.toString(modifiers) + "\"");
            }
        } else {
            throw new InternalError();
        }
    }

    private static boolean verifyMemberAccess(Class<?> caller, Class<?> declaring, int modifiers) {
        Boolean samePackage = null;

        if (caller == declaring) {
            return true;
        }

        if (!Modifier.isPublic(declaring.getModifiers())) {
            samePackage = samePackage(caller, declaring);
            if (!samePackage) {
                return false;
            }
        }
        if (Modifier.isPublic(modifiers)) {
            return true;
        } else if (Modifier.isPrivate(modifiers)) {
            return false;
        } else if (Modifier.isProtected(modifiers)) {
            if (samePackage == null) {
                samePackage = samePackage(caller, declaring);
            }
            if (samePackage) {
                return true;
            }
            if (isSubclass(declaring, caller)) {
                return true;
            }
            return false;
        } else {
            if (samePackage == null) {
                samePackage = samePackage(caller, declaring);
            }
            return samePackage;
        }
    }

    private static boolean samePackage(Class<?> c1, Class<?> c2) {
        if (c1.getClassLoader() != c2.getClassLoader()) {
            return false;
        } else {
            String name1 = c1.getName();
            String name2 = c2.getName();
            int dotPos1 = name1.lastIndexOf('.');
            int dotPos2 = name2.lastIndexOf('.');
            if (dotPos1 == -1 && dotPos2 == -1) {
                return true; //both have no package
            } else if (dotPos1 == -1 || dotPos2 == -1) {
                return false; //one has no package
            } else {
                int start1 = 0;
                int start2 = 0;
                while (name1.charAt(start1) == '[') {
                    ++start1;
                }
                while (name2.charAt(start2) == '[') {
                    ++start2;
                }
                int l1 = dotPos1 - start1;
                int l2 = dotPos1 - start1;
                if (l1 != l2) {
                    return false;
                }
                return name1.regionMatches(false, start1, name2, start2, l2);
            }
        }
    }

    private static boolean isSubclass(Class<?> candidate, Class<?> target) {
        while (candidate != null) {
            if (candidate == target) {
                return true;
            }

            candidate = candidate.getSuperclass();
        }

        return false;
    }

    static Class<?> getCallerClass(int pos) {
        return GET_CALLER.apply(pos + 1);
    }
}
