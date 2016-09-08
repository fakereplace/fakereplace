package org.fakereplace.manip.data;

import org.fakereplace.manip.util.ClassLoaderFiltered;

/**
 * @author Stuart Douglas
 */
public class FakeMethodCallData implements ClassLoaderFiltered<FakeMethodCallData> {
    private final String className;
    private final String methodName;
    private final String methodDesc;
    private final Type type;
    private final ClassLoader classLoader;
    private final int methodNumber;

    public FakeMethodCallData(String className, String methodName, String methodDesc, Type type, ClassLoader classLoader, int methodNumber) {
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.type = type;
        this.classLoader = classLoader;
        this.methodNumber = methodNumber;
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

        FakeMethodCallData that = (FakeMethodCallData) o;

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

    public FakeMethodCallData getInstance() {
        return this;
    }

    public enum Type {
        VIRTUAL, STATIC, INTERFACE
    }
}
