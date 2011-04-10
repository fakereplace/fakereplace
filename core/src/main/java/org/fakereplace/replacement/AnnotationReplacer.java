package org.fakereplace.replacement;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import org.fakereplace.data.AnnotationBuilder;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.ClassDataBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AnnotationReplacer {

    /**
     * Stores class file annotations changes and reverts the file annotations to
     * the old annotations
     *
     * @param file
     * @param old
     * @param builder
     */
    public static void processAnnotations(ClassFile file, Class old, ClassDataBuilder builder) {

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
