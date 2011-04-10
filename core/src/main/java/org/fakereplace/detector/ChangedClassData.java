package org.fakereplace.detector;

public class ChangedClassData {

    private final Class<?> javaClass;
    private final byte[] classFile;

    public ChangedClassData(Class<?> javaClass, byte[] classFile) {
        this.javaClass = javaClass;
        this.classFile = classFile;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public byte[] getClassFile() {
        return classFile;
    }

}
