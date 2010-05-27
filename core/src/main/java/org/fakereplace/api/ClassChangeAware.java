package org.fakereplace.api;

/**
 * interface that should be implemented by classes that with to be notified of class changes
 * @author stuart
 *
 */
public interface ClassChangeAware
{
   public void beforeChange(Class<?>[] changed, Class<?>[] added);
   
   public void notify(Class<?>[] changed, Class<?>[] added);
}
