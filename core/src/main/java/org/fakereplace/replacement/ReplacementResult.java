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

import java.lang.instrument.ClassDefinition;
import java.util.Set;

public class ReplacementResult {
    private final ClassDefinition[] classes;
    private final Set<Class<?>> classesToRetransform;

    public ReplacementResult(ClassDefinition[] classes, Set<Class<?>> classesToRetransform) {
        this.classes = classes;
        this.classesToRetransform = classesToRetransform;
    }

    public ClassDefinition[] getClasses() {
        return classes;
    }

    public Set<Class<?>> getClassesToRetransform() {
        return classesToRetransform;
    }
}
