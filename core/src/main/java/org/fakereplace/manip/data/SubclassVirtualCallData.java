package org.fakereplace.manip.data;

import org.fakereplace.manip.util.ClassloaderFiltered;

public class SubclassVirtualCallData implements ClassloaderFiltered<SubclassVirtualCallData> {

    private final ClassLoader classLoader;
    private final String className;
    private final String methodName;
    private final String methodDesc;

    public SubclassVirtualCallData(ClassLoader classLoader, String className, String methodName, String methodDesc) {
        this.classLoader = classLoader;
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    public SubclassVirtualCallData getInstane() {
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((methodDesc == null) ? 0 : methodDesc.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubclassVirtualCallData other = (SubclassVirtualCallData) obj;
        if (classLoader == null) {
            if (other.classLoader != null)
                return false;
        } else if (!classLoader.equals(other.classLoader))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (methodDesc == null) {
            if (other.methodDesc != null)
                return false;
        } else if (!methodDesc.equals(other.methodDesc))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

}
