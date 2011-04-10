package org.fakereplace.test.replacement.interfacemethod;

public class ImplementingClass1 implements SomeInterface1 {

    public String added() {
        return "added";
    }

    public String existing() {
        return "newexisting";
    }

}
