package org.fakereplace.api;

import java.lang.instrument.ClassFileTransformer;
import java.util.Set;

/**
 * Integrations need to implementent this service to tell the transformer
 * about what they need. 
 * 
 * Note: all class names should be returned in java (not JVM) format 
 * 
 * @author stuart
 *
 */
public interface IntegrationInfo
{
   /**
    * Integrations have a change to transform classes
    * They get to see the class before any manipulation is
    * done to it. 
    * They do not get to transform reloaded classes.
    * @return
    */
   public ClassFileTransformer getTransformer();

   /**
    * If a classloader loads one of these classes it gets registered as
    * an integration class. This means that the classloader that loaded
    * the class will be intrumented to load classes from the integration.
    */
   public Set<String> getIntegrationClassNames();

   /**
    * return a class definition that can be loaded into the same classloader
    * that is running the app 
    */
   public byte[] loadClass(String className);

   /**
    * get a list of classes that should be turned into tracked instrances. 
    * 
    * @return
    */
   public Set<String> getTrackedInstanceClassNames();
}
