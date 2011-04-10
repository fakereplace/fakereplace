package org.fakereplace.boot;

import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.manip.util.MapFunction;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class holds proxy definitions, that are later loaded by the relevant ClassLoaders
 *
 * @author stuart
 */
public class ProxyDefinitionStore {
    static Map<ClassLoader, Map<String, byte[]>> proxyDefinitions = new MapMaker().weakKeys().makeComputingMap(new MapFunction(false));

    static AtomicLong proxyNo = new AtomicLong();

    public static byte[] getProxyDefinition(ClassLoader classLoader, String name) {
        Map<String, byte[]> def = proxyDefinitions.get(classLoader);
        return def.get(name);
    }

    public static void saveProxyDefinition(ClassLoader classLoader, String className, byte[] data) {
        Map<String, byte[]> def = proxyDefinitions.get(classLoader);
        def.put(className, data);
    }

    /**
     * Returns a unique proxy name
     *
     * @return
     */
    public static String getProxyName() {
        return Constants.GENERATED_CLASS_PACKAGE + ".ProxyClass" + proxyNo.incrementAndGet();
    }

}
