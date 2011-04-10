package org.fakereplace.test.replacement.annotated.inherited;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NotInheritedAnnotation {

}
