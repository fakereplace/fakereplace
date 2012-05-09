package org.fakereplace.api;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public interface ChangedClass extends AnnotationTarget {
    Set<ChangedAnnotation> getChangedClassAnnotations();

    Set<ChangedAnnotation> getChangedAnnotationsByType(Class<? extends Annotation> annotationType);

    Set<Changed<ChangedField>> getFields();

    Set<Changed<ChangedMethod>> getMethods();

    Class<?> getChangedClass();
}
