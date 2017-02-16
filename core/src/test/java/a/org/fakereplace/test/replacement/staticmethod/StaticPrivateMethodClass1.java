package a.org.fakereplace.test.replacement.staticmethod;

public class StaticPrivateMethodClass {
    public static String callPrivateMethod() {
        return getValue();
    }

    private static String getValue() {
        return "one";
    }
}
