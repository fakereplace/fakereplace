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

package org.fakereplace.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javassist.bytecode.AccessFlag;
import org.fakereplace.util.DescriptorUtils;

/**
 * Class that holds information about a method.
 *
 * @author stuart
 */
public class MethodData {
    private final String methodName;
    private final String descriptor;

    /**
     * contains the argument portion of the descriptor minus the return type
     */
    private final String argumentDescriptor;
    /**
     * the return type of the method
     * final
     */
    private final String returnTypeDescriptor;

    private final MemberType type;
    private final int accessFlags;
    /**
     * stores the method no for a fake method. This is only used for constructors
     */
    private final int methodNo;

    /**
     * The actual class that the method resides in java not internal format
     */
    private final String className;

    private final boolean finalMethod;

    public MethodData(String name, String descriptor, String className, MemberType type, int accessFlags, boolean finalMethod) {
        this.methodName = name;
        this.descriptor = descriptor;
        this.returnTypeDescriptor = DescriptorUtils.getReturnType(descriptor);
        this.argumentDescriptor = DescriptorUtils.getArgumentString(descriptor);
        this.className = className;
        this.type = type;
        this.accessFlags = accessFlags;
        this.methodNo = 0;
        this.finalMethod = finalMethod;
    }

    public MethodData(String name, String descriptor, String className, MemberType type, int accessFlags, int methodNo) {
        this.methodName = name;
        this.descriptor = descriptor;
        this.returnTypeDescriptor = DescriptorUtils.getReturnType(descriptor);
        this.argumentDescriptor = DescriptorUtils.getArgumentString(descriptor);
        this.className = className;
        this.type = type;
        this.accessFlags = accessFlags;
        this.methodNo = methodNo;
        this.finalMethod = false;
    }

    /**
     * MethodData's are equal if they refer to the same method
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MethodData) {
            MethodData m = (MethodData) obj;
            if (m.className.equals(className)) {
                if (m.methodName.equals(methodName)) {
                    if (m.descriptor.equals(descriptor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (className + methodName + descriptor).hashCode();
    }

    public String getClassName() {
        return className;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public MemberType getType() {
        return type;
    }

    public boolean isStatic() {
        return (accessFlags & AccessFlag.STATIC) != 0;
    }

    public String getArgumentDescriptor() {
        return argumentDescriptor;
    }

    public Method getMethod(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Class<?>[] methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
        Method method = actualClass.getDeclaredMethod(methodName, methodDesc);
        return method;

    }

    public Method getMethodToInvoke(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Class<?>[] methodDesc;
        if (type == MemberType.FAKE && !isStatic()) {
            methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
            Class<?>[] ret = new Class<?>[methodDesc.length + 1];
            ret[0] = ClassDataStore.instance().getRealClassFromProxyName(actualClass.getName());
            System.arraycopy(methodDesc, 0, ret, 1, methodDesc.length);
            methodDesc = ret;
        } else {
            methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
        }
        Method method = actualClass.getDeclaredMethod(methodName, methodDesc);
        return method;

    }

    /**
     * If this method is actually a constructor get the construtor object
     */
    public Constructor<?> getConstructor(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        Class<?>[] methodDesc = DescriptorUtils.argumentStringToClassArray(descriptor, actualClass);
        Constructor<?> method = actualClass.getDeclaredConstructor(methodDesc);
        return method;
    }

    public int getMethodNo() {
        return methodNo;
    }

    public String getReturnTypeDescriptor() {
        return returnTypeDescriptor;
    }

    public boolean isFinalMethod() {
        return finalMethod;
    }

    public boolean isConstructor() {
        return methodName.equals("<init>");
    }
}
