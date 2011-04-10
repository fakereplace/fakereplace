package org.fakereplace.api;

import java.util.Set;

/**
 * Integrations need to implementent this service to tell the transformer
 * about what they need.
 * <p/>
 * Note: all class names should be returned in java (not JVM) format
 *
 * @author stuart
 */
public interface IntegrationInfo {
    /**
     * Integrations have a change to transform classes
     * They get to see the class before any manipulation is
     * done to it.
     * They do not get to transform reloaded classes.
     *
     * @return
     */
    public ClassTransformer getTransformer();

    /**
     * returns the name of the ClassChangeAware object
     * provided by this integration.
     * <p/>
     * This object is installed into the same ClassLoader
     * that the integrations classes are in
     * <p/>
     * Note that the ClassChangeAware object should register
     * itself with the ClassChangeNotifier
     *
     * @return
     */
    public String getClassChangeAwareName();

    /**
     * If a classloader loads one of these classes it enables
     * this integration module.
     * <p/>
     * This also means that the classloader that loaded
     * the class will be intrumented to load classes from the integration.
     */
    public Set<String> getIntegrationTriggerClassNames();

    /**
     * If a classloader is registered as an instrumentation
     * classloader it will attempt to load classes from here
     * first.
     */
    public byte[] loadClass(String className);

    /**
     * get a list of classes that should be turned into tracked instances.
     *
     * @return
     */
    public Set<String> getTrackedInstanceClassNames();
}
