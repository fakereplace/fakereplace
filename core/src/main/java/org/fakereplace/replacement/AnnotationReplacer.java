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

package org.fakereplace.replacement;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import org.fakereplace.data.AnnotationBuilder;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

class AnnotationReplacer {

    static AnnotationsAttribute duplicateAnnotationsAttribute(ConstPool cp, AnnotatedElement element) {
        AnnotationsAttribute oldAns = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
        Arrays.stream(element.getAnnotations())
                .forEach(annotation -> oldAns.addAnnotation(AnnotationBuilder.createJavassistAnnotation(annotation, cp)));
        return oldAns;
    }

    static ParameterAnnotationsAttribute duplicateParameterAnnotationsAttribute(ConstPool cp, Method method) {
        return duplicateParameterAnnotationsAttribute(cp, (Executable) method);
    }

    static ParameterAnnotationsAttribute duplicateParameterAnnotationsAttribute(ConstPool cp, Constructor<?> method) {
        return duplicateParameterAnnotationsAttribute(cp, (Executable) method);
    }

    private static ParameterAnnotationsAttribute duplicateParameterAnnotationsAttribute(ConstPool cp, Executable method) {
        ParameterAnnotationsAttribute oldAns = new ParameterAnnotationsAttribute(cp, ParameterAnnotationsAttribute.visibleTag);
        Annotation[][] anAr = new Annotation[method.getParameterAnnotations().length][];
        for (int i = 0; i < anAr.length; ++i) {
            anAr[i] = new Annotation[method.getParameterAnnotations()[i].length];
            for (int j = 0; j < anAr[i].length; ++j) {
                anAr[i][j] = AnnotationBuilder.createJavassistAnnotation(method.getParameterAnnotations()[i][j], cp);
            }
        }
        oldAns.setAnnotations(anAr);
        return oldAns;
    }
}
