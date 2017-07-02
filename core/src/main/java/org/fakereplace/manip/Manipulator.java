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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

/**
 * Class that maintains a set of manipulations to apply to classes
 *
 * @author stuart
 */
public class Manipulator {

    private final VirtualToStaticManipulator virtualToStaticManipulator = new VirtualToStaticManipulator();
    private final FieldManipulator instanceFieldManapulator = new FieldManipulator();
    private final ConstructorInvocationManipulator constructorInvocationManipulator = new ConstructorInvocationManipulator();
    private final ReflectionConstructorAccessManipulator reflectionConstructorAccessManipulator = new ReflectionConstructorAccessManipulator();
    private final SubclassVirtualCallManipulator subclassVirtualCallManilulator = new SubclassVirtualCallManipulator();
    private final FinalMethodManipulator finalMethodManipulator = new FinalMethodManipulator();
    private final ReflectionFieldAccessManipulator reflectionFieldAccessManipulator = new ReflectionFieldAccessManipulator();
    private final ReflectionMethodAccessManipulator reflectionMethodAccessManipulator = new ReflectionMethodAccessManipulator();
    private final FakeMethodCallManipulator fakeMethodCallManipulator = new FakeMethodCallManipulator();

    private final Set<ClassManipulator> manipulators = new CopyOnWriteArraySet<>();

    public Manipulator() {
        manipulators.add(virtualToStaticManipulator);
        manipulators.add(instanceFieldManapulator);
        manipulators.add(constructorInvocationManipulator);
        manipulators.add(subclassVirtualCallManilulator);
        manipulators.add(finalMethodManipulator);
        manipulators.add(reflectionFieldAccessManipulator);
        manipulators.add(reflectionMethodAccessManipulator);
        manipulators.add(reflectionConstructorAccessManipulator);
        manipulators.add(fakeMethodCallManipulator);
    }

    public void removeRewrites(String className, ClassLoader classLoader) {
        for (ClassManipulator m : manipulators) {
            m.clearRewrites(className, classLoader);
        }
    }

    public void rewriteConstructorAccess(String clazz, String descriptor, int methodNo, ClassLoader classLoader) {
        constructorInvocationManipulator.rewriteConstructorCalls(clazz, descriptor, methodNo, classLoader);
    }

    public void rewriteInstanceFieldAccess(FieldManipulator.AddedFieldData data) {
        instanceFieldManapulator.addField(data);
    }

    public void rewriteSubclassCalls(String className, ClassLoader classLoader, String parentName, ClassLoader parentClassLoader, String methodName, String methodDesc) {
        subclassVirtualCallManilulator.addClassData(className, classLoader, parentName, parentClassLoader, methodName, methodDesc);
    }

    /**
     * This can also be used to replace a static invokation with another static
     * invokation
     *
     */
    public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        virtualToStaticManipulator.replaceVirtualMethodInvokationWithStatic(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, classLoader);
    }

    public void replaceVirtualMethodInvokationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        virtualToStaticManipulator.replaceVirtualMethodInvokationWithLocal(oldClass, methodName, newMethodName, methodDesc, newStaticMethodDesc, classLoader);
    }

    public void addFakeMethodCallRewrite(FakeMethodCallManipulator.FakeMethodCallData fakeMethodCallData) {
        fakeMethodCallManipulator.addFakeMethodCall(fakeMethodCallData);
    }

    public boolean transformClass(ClassFile file, ClassLoader classLoader, boolean modifiable, Set<MethodInfo> modifiedMethods) {
        try {
            boolean modified = false;

            // first we are going to transform virtual method calls to static ones
            for (ClassManipulator m : manipulators) {
                if (m.transformClass(file, classLoader, modifiable, modifiedMethods)) {
                    modified = true;
                }
            }
            return modified;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
