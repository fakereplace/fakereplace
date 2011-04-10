package org.fakereplace.test.coverage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Coverage {
    ChangeTestType test();

    CodeChangeType change();

    boolean privateMember();
}
