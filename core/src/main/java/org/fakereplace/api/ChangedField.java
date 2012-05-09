package org.fakereplace.api;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public interface ChangedField {
    String getName();

    Class<?> getFieldType();

    Type getGenericType();

    int getModifiers();

    Set<ChangedAnnotation> getChangedAnnotations();
}
