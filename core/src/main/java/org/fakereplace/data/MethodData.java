package org.fakereplace.data;

import javassist.bytecode.AccessFlag;
import org.fakereplace.util.DescriptorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Class that holds information about a method.
 *
 * @author stuart
 */
public class MethodData {
    final private String methodName;
    final private String descriptor;

    /**
     * contains the argument portion of the descriptor minus the return type
     */
    final private String argumentDescriptor;
    /**
     * the return type of the method
     * final
     */
    final private String returnTypeDescriptor;

    final private MemberType type;
    final private int accessFlags;
    /**
     * stores the method no for a fake method. This is only used for constructors
     */
    final private int methodNo;

    /**
     * The actual class that the method resides in java not internal format
     */
    final private String className;

    final private boolean finalMethod;

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
            ret[0] = ClassDataStore.getRealClassFromProxyName(actualClass.getName());
            for (int i = 0; i < methodDesc.length; ++i) {
                ret[i + 1] = methodDesc[i];
            }
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

}
