package org.fakereplace.test.replacement.staticmethod.visibility.otherpackage;

public class StaticMethodVisibilityClass1 {
    public static String method() {
        return "helo world";
    }

    public static String callingMethod() {
        return method();
    }
}
