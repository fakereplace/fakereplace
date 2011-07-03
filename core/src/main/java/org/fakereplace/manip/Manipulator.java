/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.manip;

import javassist.bytecode.ClassFile;
import org.fakereplace.manip.data.AddedFieldData;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Class that maintains a set of manipulations to apply to classes
 *
 * @author stuart
 */
public class Manipulator {

    final private MethodInvokationManipulator methodInvokationManipulator = new MethodInvokationManipulator();
    final private StaticFieldManipulator staticFieldManipulator = new StaticFieldManipulator();
    final private InstanceFieldManipulator instanceFieldManapulator = new InstanceFieldManipulator();
    final private ConstructorInvocationManipulator constructorInvocationManipulator = new ConstructorInvocationManipulator();
    final private ConstructorAccessManipulator constructorAccessManipulator = new ConstructorAccessManipulator();
    final private SubclassVirtualCallManipulator subclassVirtualCallManilulator = new SubclassVirtualCallManipulator();
    final private FinalMethodManipulator finalMethodManipulator = new FinalMethodManipulator();
    final private FieldAccessManipulator fieldAccessManipulator = new FieldAccessManipulator();
    final private MethodAccessManipulator methodAccessManipulator = new MethodAccessManipulator();

    final private Set<ClassManipulator> manipulators = new CopyOnWriteArraySet<ClassManipulator>();

    public Manipulator() {
        manipulators.add(methodInvokationManipulator);
        manipulators.add(staticFieldManipulator);
        manipulators.add(instanceFieldManapulator);
        manipulators.add(constructorInvocationManipulator);
        manipulators.add(subclassVirtualCallManilulator);
        manipulators.add(finalMethodManipulator);
        manipulators.add(fieldAccessManipulator);
        manipulators.add(methodAccessManipulator);
        manipulators.add(constructorAccessManipulator);
    }

    public void removeRewrites(String className, ClassLoader classLoader) {
        for (ClassManipulator m : manipulators) {
            m.clearRewrites(className, classLoader);
        }
    }

    /**
     * rewrites static field access to the same field on another class
     *
     * @param oldClass
     * @param newClass
     * @param fieldName
     */
    public void rewriteStaticFieldAccess(String oldClass, String newClass, String fieldName, ClassLoader classLoader) {
        staticFieldManipulator.rewriteStaticFieldAccess(oldClass, newClass, fieldName, classLoader);
    }

    public void rewriteConstructorAccess(String clazz, String descriptor, int methodNo, ClassLoader classLoader) {
        constructorInvocationManipulator.rewriteConstructorCalls(clazz, descriptor, methodNo, classLoader);
    }

    public void rewriteInstanceFieldAccess(AddedFieldData data) {
        instanceFieldManapulator.addField(data);
    }

    public void rewriteSubclassCalls(String className, ClassLoader classLoader, String parentName, ClassLoader parentClassLoader, String methodName, String methodDesc) {
        System.out.println("CLS: " + className + " m " + methodName);
        subclassVirtualCallManilulator.addClassData(className, classLoader, parentName, parentClassLoader, methodName, methodDesc);
    }

    /**
     * This can also be used to replace a static invokation with another static
     * invokation
     *
     * @param oldClass
     * @param newClass
     * @param methodName
     * @param methodDesc
     * @param newStaticMethodDesc
     */
    public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, classLoader);
    }

    public void replaceVirtualMethodInvokationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader) {
        methodInvokationManipulator.replaceVirtualMethodInvokationWithLocal(oldClass, methodName, newMethodName, methodDesc, newStaticMethodDesc, classLoader);
    }

    public boolean transformClass(ClassFile file, ClassLoader classLoader, boolean modifiable) {
        boolean modified = false;

        // first we are going to transform virtual method calls to static ones
        for (ClassManipulator m : manipulators) {
            if (m.transformClass(file, classLoader, modifiable)) {
                modified = true;
            }
        }
        return modified;
    }

}
