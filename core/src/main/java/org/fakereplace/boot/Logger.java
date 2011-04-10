package org.fakereplace.boot;

/**
 * Logging class, at the moment just writes to stdout
 *
 * @author stuart
 */
public class Logger {

    public static void log(Object invoker, String message) {
        Class c = null;
        if (invoker.getClass().isAssignableFrom(Class.class)) {
            c = (Class) invoker;
        } else {
            c = invoker.getClass();
        }
        System.out.println("[" + c.getCanonicalName() + "] " + message);
    }

    public static void debug(Object invoker, String message) {
        log(invoker, message);
    }

}
