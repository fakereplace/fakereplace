package org.fakereplace.test.replacement.annotated.method;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MethodAnnotation {
    String value();
}
