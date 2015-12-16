package org.fakereplace.api;

import javassist.bytecode.ClassFile;

/**
 * @author Stuart Douglas
 */
public class NewClassData {

    private final String className;
    private final ClassLoader classLoader;
    private final ClassFile classFile;

    public NewClassData(String className, ClassLoader classLoader, ClassFile classFile) {
        this.className = className;
        this.classLoader = classLoader;
        this.classFile = classFile;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ClassFile getClassFile() {
        return classFile;
    }
}
