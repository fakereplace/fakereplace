/**
 *
 */
package org.fakereplace.classloading;

public class ClassIdentifier {
    private final String className;
    private final ClassLoader loader;

    public ClassIdentifier(String className, ClassLoader loader) {
        this.className = className;
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassIdentifier) {
            ClassIdentifier c = (ClassIdentifier) obj;
            return c.loader == loader && c.className.equals(className);
        }
        return false;
    }

}