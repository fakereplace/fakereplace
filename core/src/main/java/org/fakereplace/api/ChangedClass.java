/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.api;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class ChangedClass implements AnnotationTarget {

    private final Set<ChangedAnnotation> changedClassAnnotations;
    private final Map<Class<? extends Annotation>, Set<ChangedAnnotation>> changedAnnotationsByType;
    private final Set<Changed<ChangedField>> fields;
    private final Set<Changed<ChangedMethod>> methods;
    private final Class<?> changedClass;

    public ChangedClass(final Set<Changed<ChangedMethod>> methods, final Set<Changed<ChangedField>> fields, final Set<ChangedAnnotation> changedClassAnnotations, final Map<Class<? extends Annotation>, Set<ChangedAnnotation>> changedAnnotationsByType, Class<?> changedClass) {
        this.methods = methods;
        this.fields = fields;
        this.changedClassAnnotations = changedClassAnnotations;
        this.changedAnnotationsByType = changedAnnotationsByType;
        this.changedClass = changedClass;
    }

    /**
     *
     * @return The changed class level annotations
     */
    public Set<ChangedAnnotation> getChangedClassAnnotations() {
        return Collections.unmodifiableSet(changedClassAnnotations);
    }

    /**
     * Get all changed annotations of a certain type.
     * This includes field and method level annotations.
     * @param annotationType The type of annotation
     * @return All changed annotations of a specific type
     */
    public Set<ChangedAnnotation> getChangedAnnotationsByType(final Class<? extends Annotation> annotationType) {
        return Collections.unmodifiableSet(changedAnnotationsByType.get(annotationType));
    }

    public Set<Changed<ChangedField>> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    public Set<Changed<ChangedMethod>> getMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public Class<?> getChangedClass() {
        return changedClass;
    }
}
