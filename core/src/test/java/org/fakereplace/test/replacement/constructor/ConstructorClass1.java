package org.fakereplace.test.replacement.constructor;

import org.fakereplace.util.NoInstrument;

import java.util.List;

@NoInstrument
public class ConstructorClass1 {

    public ConstructorClass1(String a) {
        value = a;
    }

    private ConstructorClass1(List<String> a) {
        value = "h";
    }

    public ConstructorClass1(int i1, int i2, int i3, int i4, int i5, int i6) {
        value = "h";
    }

    String value = "a";

    public String getValue() {
        return value;
    }
}
