package org.fakereplace.test.replacement.annotated.field;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldAnnotation
{
   String value();
}
