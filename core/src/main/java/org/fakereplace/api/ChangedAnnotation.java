package org.fakereplace.api;

import java.lang.annotation.Annotation;

/**
 * @author Stuart Douglas
 */
public interface ChangedAnnotation extends Changed<Annotation> {
    AnnotationTarget getAnnotationTarget();

    Class<? extends Annotation> getAnnotationType();
}
