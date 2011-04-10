package org.fakereplace.test.replacement.annotated.method;

public class MethodParameterAnnotated {
    public void method1(int a) {

    }

    public void method2(@MethodAnnotation("2") int a) {

    }
}
