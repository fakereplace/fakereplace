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

package org.fakereplace.manip;

import java.util.Set;

import org.fakereplace.core.Constants;
import org.fakereplace.data.ModifiedMethod;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * manipulator that removes the final attribute from methods
 *
 * @author Stuart Douglas
 */
class FinalMethodManipulator implements ClassManipulator {

    public void clearRewrites(String className, ClassLoader classLoader) {

    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods, boolean replaceable) {
        if (!modifiableClass) {
            return false;
        }
        boolean modified = false;

        for (Object i : file.getMethods()) {
            MethodInfo m = (MethodInfo) i;
            if ((m.getAccessFlags() & AccessFlag.FINAL) != 0) {
                m.setAccessFlags(m.getAccessFlags() & ~AccessFlag.FINAL);
                // ClassDataStore.addFinalMethod(file.getName(), m.getName(),
                // m.getDescriptor());
                AnnotationsAttribute at = (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag);
                if (at == null) {
                    at = new AnnotationsAttribute(file.getConstPool(), AnnotationsAttribute.visibleTag);
                    m.addAttribute(at);
                }
                at.addAnnotation(new Annotation(ModifiedMethod.class.getName(), file.getConstPool()));
                m.addAttribute(new AttributeInfo(file.getConstPool(), Constants.FINAL_METHOD_ATTRIBUTE, new byte[0]));
                modified = true;
            }
        }
        return modified;
    }

}
