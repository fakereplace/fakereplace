package org.fakereplace.replacement;

import org.fakereplace.classloading.ClassIdentifier;

public class AddedClass {
    private final String className;
    private final byte[] data;
    private final ClassLoader loader;

    public AddedClass(String className, byte[] data, ClassLoader loader) {
        this.className = className;
        this.data = data;
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    public byte[] getData() {
        return data;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public ClassIdentifier getClassIdentifier() {
        return new ClassIdentifier(className, loader);
    }
}
