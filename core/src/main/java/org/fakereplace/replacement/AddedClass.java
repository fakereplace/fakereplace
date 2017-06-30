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

public class AddedClass {
    private final String className;
    private final byte[] data;
    private final ClassLoader loader;

    public AddedClass(String className, byte[] data, ClassLoader loader) {
        this.className = className;
        this.data = data;
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    public byte[] getData() {
        return data;
    }

    public ClassLoader getLoader() {
        return loader;
    }

}
