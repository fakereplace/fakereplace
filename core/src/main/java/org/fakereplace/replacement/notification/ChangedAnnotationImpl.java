/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.fakereplace.replacement.notification;

import org.fakereplace.api.AnnotationTarget;
import org.fakereplace.api.ChangeType;
import org.fakereplace.api.ChangedAnnotation;

import java.lang.annotation.Annotation;

/**
 * A modified annotation
 *
 * @author Stuart Douglas
 */
public class ChangedAnnotationImpl extends ChangedImpl<Annotation> implements ChangedAnnotation {

    private final Class<? extends  Annotation> annotationType;
    private final AnnotationTarget annotationTarget;

    public ChangedAnnotationImpl(Annotation modified, Annotation existing, ChangeType type, AnnotationTarget annotationTarget, final Class<? extends Annotation> annotationType) {
        super(modified, existing, type);
        this.annotationTarget = annotationTarget;
        this.annotationType = annotationType;
    }

    @Override
    public AnnotationTarget getAnnotationTarget() {
        return annotationTarget;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }
}
