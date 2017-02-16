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
package org.fakereplace.transformation;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.fakereplace.replacement.notification.ChangedClassImpl;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;

/**
 * @author Stuart Douglas
 */
public interface FakereplaceTransformer {


    /**
     * Transforms a class, returning true if any modifications where made
     *
     * @param classesToRetransform implementation should add classes to this set that are needed to retransform by agent
     *                             with {@link java.lang.instrument.Instrumentation#retransformClasses(Class[])}
     * @param modifiedMethods      implementation should add methods that are changed to this set
     */
    boolean transform(final ClassLoader loader,
                      final String className,
                      final Class<?> classBeingRedefined,
                      final ProtectionDomain protectionDomain,
                      final ClassFile file,
                      Set<Class<?>> classesToRetransform,
                      ChangedClassImpl changedClass,
                      Set<MethodInfo> modifiedMethods)
            throws IllegalClassFormatException, BadBytecode, DuplicateMemberException;

}
