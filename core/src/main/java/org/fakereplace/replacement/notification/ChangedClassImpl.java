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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fakereplace.api.ChangedAnnotation;
import org.fakereplace.api.ChangedClass;

/**
 * @author Stuart Douglas
 */
public class ChangedClassImpl implements ChangedClass {

    private final Map<Class<? extends Annotation>, Set<ChangedAnnotation>> changedAnnotationsByType = new HashMap<>();
    private final Class<?> changedClass;

    public ChangedClassImpl(final Class<?> changedClass) {
        this.changedClass = changedClass;
    }

    /**
     * Get all changed annotations of a certain type.
     * This includes field and method level annotations.
     * @param annotationType The type of annotation
     * @return All changed annotations of a specific type
     */
    @Override
    public Set<ChangedAnnotation> getChangedAnnotationsByType(final Class<? extends Annotation> annotationType) {
        final Set<ChangedAnnotation> changedAnnotations = changedAnnotationsByType.get(annotationType);
        if(changedAnnotations == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(changedAnnotations);
    }

    @Override
    public Class<?> getChangedClass() {
        return changedClass;
    }

    public void changeClassAnnotation(final ChangedAnnotation annotation) {
        changedAnnotation(annotation);
    }


    private void changedAnnotation(final ChangedAnnotation annotation) {
        Set<ChangedAnnotation> set = changedAnnotationsByType.computeIfAbsent(annotation.getAnnotationType(), k -> new HashSet<>());
        set.add(annotation);
    }

    @Override
    public String toString() {
        return "ChangedClassImpl{" +
                ", changedAnnotationsByType=" + changedAnnotationsByType +
                ", changedClass=" + changedClass +
                '}';
    }
}
