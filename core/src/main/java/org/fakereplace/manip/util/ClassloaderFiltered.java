package org.fakereplace.manip.util;

/**
 * Represents an object that can be filtered by classloader
 * 
 * 
 * @author stuart
 *
 * @param <T>
 */
public interface ClassloaderFiltered<T>
{
   public ClassLoader getClassLoader();

   public T getInstane();
}
