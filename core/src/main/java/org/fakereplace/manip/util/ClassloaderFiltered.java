package org.fakereplace.manip.util;

/**
 * Represents an object that can be filtered by classloader
 *
 * @param <T>
 * @author stuart
 */
public interface ClassloaderFiltered<T> {
    public ClassLoader getClassLoader();

    public T getInstane();
}
