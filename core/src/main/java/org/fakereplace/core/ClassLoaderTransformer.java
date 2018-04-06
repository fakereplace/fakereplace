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

package org.fakereplace.core;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.fakereplace.replacement.notification.ChangedClassImpl;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

/**
 * transformer that instruments class loaders to load FakeReplace classes
 *
 * @author stuart
 */
class ClassLoaderTransformer implements FakereplaceTransformer {

    @Override
    public boolean transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods, boolean replaceable) throws BadBytecode {
        if (classBeingRedefined != null && ClassLoader.class.isAssignableFrom(classBeingRedefined)) {
            return ClassLoaderInstrumentation.redefineClassLoader(file, modifiedMethods);
        } else if (classBeingRedefined == null && className != null && className.endsWith("ClassLoader")) { //TODO: fix this
            return ClassLoaderInstrumentation.redefineClassLoader(file, modifiedMethods);
        }
        return false;
    }
}
