package org.fakereplace.test.replacement.annotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Annotation1 {
    String svalue();

    Class cvalue();
}
