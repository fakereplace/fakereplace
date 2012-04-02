/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.transformation;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.bytecode.ClassFile;
import org.fakereplace.ClassLoaderInstrumentation;

/**
 * transformer that instruments class loaders to load FakeReplace classes
 *
 * @author stuart
 */
public class ClassLoaderTransformer implements FakereplaceTransformer {


    public ClassLoaderTransformer() {

    }

    @Override
    public boolean transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final ClassFile file) throws IllegalClassFormatException {

        try {
            if (classBeingRedefined != null && ClassLoader.class.isAssignableFrom(classBeingRedefined)) {
                return ClassLoaderInstrumentation.redefineClassLoader(file);
            } else if (classBeingRedefined == null && className.endsWith("ClassLoader")) {
                return ClassLoaderInstrumentation.redefineClassLoader(file);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalClassFormatException();
        }
        return false;
    }
}
