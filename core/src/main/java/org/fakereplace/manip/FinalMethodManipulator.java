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

package org.fakereplace.manip;

import java.util.Set;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import org.fakereplace.core.Constants;
import org.fakereplace.data.ModifiedMethod;

/**
 * manipulator that removes the final attribute from methods
 *
 * @author Stuart Douglas <stuart.w.douglas@gmail.com>
 */
public class FinalMethodManipulator implements ClassManipulator {

    public void clearRewrites(String className, ClassLoader classLoader) {

    }

    public boolean transformClass(ClassFile file, ClassLoader loader, boolean modifiableClass, final Set<MethodInfo> modifiedMethods) {
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
