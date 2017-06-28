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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fakereplace.api.ChangedAnnotation;
import org.fakereplace.api.ChangedMethod;

/**
 * @author Stuart Douglas
 */
public class ChangedMethodImpl implements ChangedMethod {

    private final String name;
    private final Class<?> returnType;
    private final Type genericReturnType;
    private final List<Class<?>> parameterTypes;
    private final List<Type> genericTypes;
    private final int modifiers;
    private final Set<ChangedAnnotation> changedAnnotations;

    public ChangedMethodImpl(List<Type> genericTypes, List<Class<?>> parameterTypes, Class<?> returnType, Set<ChangedAnnotation> changedAnnotations, int modifiers, final Type genericReturnType, final String name) {
        this.genericTypes = genericTypes;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.changedAnnotations = changedAnnotations;
        this.modifiers = modifiers;
        this.genericReturnType = genericReturnType;
        this.name = name;
    }


    @Override
    public Set<ChangedAnnotation> getChangedAnnotations() {
        return Collections.unmodifiableSet(changedAnnotations);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public Type getGenericReturnType() {
        return genericReturnType;
    }

    @Override
    public List<Class<?>> getParameterTypes() {
        return Collections.unmodifiableList(parameterTypes);
    }

    @Override
    public List<Type> getGenericTypes() {
        return Collections.unmodifiableList(genericTypes);
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }
}
