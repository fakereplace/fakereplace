package org.fakereplace.test.replacement.staticmethod.visibility.otherpackage;

public class UnchangedStaticMethodCallingClass {
    public static String callingClass() {
        return StaticMethodVisibilityClass.method();
    }
}
