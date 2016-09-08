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

package org.fakereplace.api.environment;

import java.util.Collections;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class ChangedClasses {

    public static final ChangedClasses EMPTY = new ChangedClasses(Collections.<Class<?>>emptySet(), Collections.<String>emptySet(), null);

    private final Set<Class<?>> changed;
    private final Set<String> newClasses;
    /**
     * The class loader to use for new classes
     */
    private final ClassLoader classLoader;

    public ChangedClasses(final Set<Class<?>> changed, final Set<String> newClasses, final ClassLoader classLoader) {
        this.changed = changed;
        this.newClasses = newClasses;
        this.classLoader = classLoader;
    }

    public Set<Class<?>> getChanged() {
        return changed;
    }

    public Set<String> getNewClasses() {
        return newClasses;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
