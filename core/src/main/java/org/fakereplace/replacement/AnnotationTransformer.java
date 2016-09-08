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

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.replacement.notification.ChangedClassImpl;
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
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if(classBeingRedefined != null) {
            AnnotationsAttribute newAns = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.visibleTag);
            AnnotationDataStore.recordClassAnnotations(classBeingRedefined, newAns, changedClass);
            file.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), classBeingRedefined));
        }
        return false;
    }
}
