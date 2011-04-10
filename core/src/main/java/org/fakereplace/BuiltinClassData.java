package org.fakereplace;

public class BuiltinClassData {

    static final String[] doNotInstrument = {"org/fakereplace", "java/math", "java/lang", "java/util/concurrent", "java/util/Currency", "java/util/Random",};

    static final String[] exceptions = {"java/lang/reflect/Proxy", "org/fakereplace/test",};

    public static boolean skipInstrumentation(String className) {
        className = className.replace('.', '/');
        for (String s : exceptions) {
            if (className.startsWith(s)) {
                return false;
            }
        }
        for (String s : doNotInstrument) {
            if (className.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
