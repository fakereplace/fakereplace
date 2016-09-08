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

import org.fakereplace.api.ChangedAnnotation;
import org.fakereplace.api.ChangedField;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class ChangedFieldImpl implements ChangedField {

    private final String name;
    private final Class<?> fieldType;
    private final Type genericType;
    private final int modifiers;
    private final Set<ChangedAnnotation> changedAnnotations;

    public ChangedFieldImpl(Type genericType, int modifiers, Class<?> fieldType, String name, Set<ChangedAnnotation> annotations) {
        this.genericType = genericType;
        this.modifiers = modifiers;
        this.fieldType = fieldType;
        this.name = name;
        this.changedAnnotations = annotations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getFieldType() {
        return fieldType;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public Set<ChangedAnnotation> getChangedAnnotations() {
        return Collections.unmodifiableSet(changedAnnotations);
    }
}
