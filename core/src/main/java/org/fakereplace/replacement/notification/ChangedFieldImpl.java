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
