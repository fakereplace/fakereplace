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

package org.fakereplace.replacement;

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
class ChangedClassBuilder {

    private final Class<?> clazz;

    ChangedClassBuilder(final Class<?> clazz) {
        this.clazz = clazz;
    }

    private final Set<ChangedAnnotation> changedClassAnnotations = new HashSet<ChangedAnnotation>();
    private final Map<Class<? extends Annotation>, Set<ChangedAnnotation>> changedAnnotationsByType = new HashMap<Class<? extends Annotation>, Set<ChangedAnnotation>>();


    public void changeClassAnnotation(final ChangedAnnotation annotation) {
        changedClassAnnotations.add(annotation);
        changedAnnotation(annotation);
    }

    public ChangedClass build() {
        return new ChangedClass(Collections.<Changed<ChangedMethod>>emptySet(), Collections.<Changed<ChangedField>>emptySet(), changedClassAnnotations,  changedAnnotationsByType, clazz);
    }


    private void changedAnnotation(final ChangedAnnotation annotation) {
        Set<ChangedAnnotation> set = changedAnnotationsByType.get(annotation.getAnnotationType());
        if(set == null) {
            changedAnnotationsByType.put(annotation.getAnnotationType(), set = new HashSet<ChangedAnnotation>());
        }
        set.add(annotation);
    }

}
