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

package org.fakereplace.api;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Represents a class that has been replaced by Fakereplace
 *
 * @author Stuart Douglas
 */
public interface ChangedClass extends AnnotationTarget {

    /**
     *
     * @return class level annotations that were modified
     */
    Set<ChangedAnnotation> getChangedClassAnnotations();

    /**
     *
     * @param annotationType The type of annotation to check
     * @return Any changed annotations of the given type
     */
    Set<ChangedAnnotation> getChangedAnnotationsByType(Class<? extends Annotation> annotationType);

    /**
     *
     * @return Any fields that have been modified
     */
    Set<Changed<ChangedField>> getFields();

    /**
     *
     * @return Any methods that have been modified
     */
    Set<Changed<ChangedMethod>> getMethods();

    /**
     *
     * @return The Class object of the changed class. Any changes will show up in reflective operations from instrumented code
     */
    Class<?> getChangedClass();
}
