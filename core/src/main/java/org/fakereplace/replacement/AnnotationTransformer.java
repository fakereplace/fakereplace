package org.fakereplace.replacement;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.transformation.FakereplaceTransformer;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;

/**
 * @author Stuart Douglas
 */
public class AnnotationTransformer implements FakereplaceTransformer {

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if(classBeingRedefined != null) {
            AnnotationsAttribute newAns = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.visibleTag);
            AnnotationDataStore.recordClassAnnotations(classBeingRedefined, newAns);
            file.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), classBeingRedefined));
        }
        return false;
    }
}
