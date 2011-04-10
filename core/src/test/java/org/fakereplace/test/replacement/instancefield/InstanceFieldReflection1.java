package org.fakereplace.test.replacement.instancefield;

public class InstanceFieldReflection1 {

    String value = "hi";

    long longValue = 0;

    int intValue = 0;

    public int vis;

    @SomeAnnotation
    private int hid;

    public String getValue() {
        return value;
    }

    public long getLongValue() {
        return longValue;
    }

    public int getIntValue() {
        return intValue;
    }

}
