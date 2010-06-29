package org.fakereplace.api;

import org.fakereplace.classloading.ClassIdentifier;

/**
 * interface that should be implemented by classes that with to be notified of
 * class changes
 * 
 * @author stuart
 * 
 */
public interface ClassChangeAware
{
   public void beforeChange(Class<?>[] changed, ClassIdentifier[] added);

   public void notify(Class<?>[] changed, ClassIdentifier[] added);
}
