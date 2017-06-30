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

/**
 * Stores information about an added instance field.
 *
 * @author stuart
 */
public class AddedFieldData implements ClassLoaderFiltered<AddedFieldData> {
    private final int arrayIndex;
    private final String name;
    private final String descriptor;
    private final String className;
    private final ClassLoader classLoader;

    public AddedFieldData(int arrayIndex, String name, String descriptor, String className, ClassLoader classLoader) {
        super();
        this.arrayIndex = arrayIndex;
        this.name = name;
        this.descriptor = descriptor;
        this.className = className;
        this.classLoader = classLoader;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public AddedFieldData getInstance() {
        return this;
    }

}
