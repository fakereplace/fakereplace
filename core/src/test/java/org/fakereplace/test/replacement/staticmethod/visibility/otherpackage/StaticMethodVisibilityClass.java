package org.fakereplace.test.replacement.staticmethod.visibility.otherpackage;

public class StaticMethodVisibilityClass {
    static String method() {
        return "helo";
    }

    public static String callingMethod() {
        return method();
    }
}
