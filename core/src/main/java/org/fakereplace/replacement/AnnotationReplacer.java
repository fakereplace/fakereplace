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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import org.fakereplace.data.AnnotationBuilder;
import org.fakereplace.data.AnnotationDataStore;

public class AnnotationReplacer {

    /**
     * Stores class file annotations changes and reverts the file annotations to
     * the old annotations
     *
     * @param file
     * @param old
     */
    public static void processAnnotations(ClassFile file, Class old) {

        AnnotationsAttribute newAns = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.visibleTag);
        AnnotationDataStore.recordClassAnnotations(old, newAns);
        file.addAttribute(duplicateAnnotationsAttribute(file.getConstPool(), old));
    }

    public static AnnotationsAttribute duplicateAnnotationsAttribute(ConstPool cp, AnnotatedElement element) {
        AnnotationsAttribute oldAns = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
        for (Annotation a : element.getAnnotations()) {
            oldAns.addAnnotation(AnnotationBuilder.createJavassistAnnotation(a, cp));
        }
        return oldAns;
    }

    public static ParameterAnnotationsAttribute duplicateParameterAnnotationsAttribute(ConstPool cp, Method method) {
        ParameterAnnotationsAttribute oldAns = new ParameterAnnotationsAttribute(cp, ParameterAnnotationsAttribute.visibleTag);
        javassist.bytecode.annotation.Annotation[][] anAr = new javassist.bytecode.annotation.Annotation[method.getParameterAnnotations().length][];
        for (int i = 0; i < anAr.length; ++i) {
            anAr[i] = new javassist.bytecode.annotation.Annotation[method.getParameterAnnotations()[i].length];
            for (int j = 0; j < anAr[i].length; ++j) {
                anAr[i][j] = AnnotationBuilder.createJavassistAnnotation(method.getParameterAnnotations()[i][j], cp);
            }
        }
        oldAns.setAnnotations(anAr);
        return oldAns;
    }

    public static ParameterAnnotationsAttribute duplicateParameterAnnotationsAttribute(ConstPool cp, Constructor<?> method) {
        ParameterAnnotationsAttribute oldAns = new ParameterAnnotationsAttribute(cp, ParameterAnnotationsAttribute.visibleTag);
        javassist.bytecode.annotation.Annotation[][] anAr = new javassist.bytecode.annotation.Annotation[method.getParameterAnnotations().length][];
        for (int i = 0; i < anAr.length; ++i) {
            anAr[i] = new javassist.bytecode.annotation.Annotation[method.getParameterAnnotations()[i].length];
            for (int j = 0; j < anAr[i].length; ++j) {
                anAr[i][j] = AnnotationBuilder.createJavassistAnnotation(method.getParameterAnnotations()[i][j], cp);
            }
        }
        oldAns.setAnnotations(anAr);
        return oldAns;
    }
}
