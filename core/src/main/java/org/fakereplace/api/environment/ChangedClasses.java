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
