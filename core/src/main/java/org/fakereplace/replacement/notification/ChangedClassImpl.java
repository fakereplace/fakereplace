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

import org.fakereplace.api.Changed;
import org.fakereplace.api.ChangedAnnotation;
import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ChangedField;
import org.fakereplace.api.ChangedMethod;

/**
 * @author Stuart Douglas
 */
public class ChangedClassImpl implements ChangedClass {

    private final Set<ChangedAnnotation> changedClassAnnotations = new HashSet<ChangedAnnotation>();
    private final Map<Class<? extends Annotation>, Set<ChangedAnnotation>> changedAnnotationsByType = new HashMap<Class<? extends Annotation>, Set<ChangedAnnotation>>();
    private final Set<Changed<ChangedField>> fields = new HashSet<Changed<ChangedField>>();
    private final Set<Changed<ChangedMethod>> methods = new HashSet<Changed<ChangedMethod>>();
    private final Class<?> changedClass;

    public ChangedClassImpl(final Class<?> changedClass) {
        this.changedClass = changedClass;
    }

    /**
     *
     * @return The changed class level annotations
     */
    @Override
    public Set<ChangedAnnotation> getChangedClassAnnotations() {
        return Collections.unmodifiableSet(changedClassAnnotations);
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
    public Set<Changed<ChangedField>> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    @Override
    public Set<Changed<ChangedMethod>> getMethods() {
        return Collections.unmodifiableSet(methods);
    }

    @Override
    public Class<?> getChangedClass() {
        return changedClass;
    }

    public void changeClassAnnotation(final ChangedAnnotation annotation) {
        changedClassAnnotations.add(annotation);
        changedAnnotation(annotation);
    }


    private void changedAnnotation(final ChangedAnnotation annotation) {
        Set<ChangedAnnotation> set = changedAnnotationsByType.get(annotation.getAnnotationType());
        if(set == null) {
            changedAnnotationsByType.put(annotation.getAnnotationType(), set = new HashSet<ChangedAnnotation>());
        }
        set.add(annotation);
    }

    @Override
    public String toString() {
        return "ChangedClassImpl{" +
                "changedClassAnnotations=" + changedClassAnnotations +
                ", changedAnnotationsByType=" + changedAnnotationsByType +
                ", fields=" + fields +
                ", methods=" + methods +
                ", changedClass=" + changedClass +
                '}';
    }
}
