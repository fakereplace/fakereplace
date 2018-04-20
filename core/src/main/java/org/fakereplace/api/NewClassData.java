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

package org.fakereplace.api;

import javassist.bytecode.ClassFile;

/**
 * Represents a new class
 *
 * @author Stuart Douglas
 */
public class NewClassData {

    private final String className;
    private final ClassLoader classLoader;
    private final ClassFile classFile;
    private final byte[] data;

    public NewClassData(String className, ClassLoader classLoader, ClassFile classFile, byte[] data) {
        this.className = className;
        this.classLoader = classLoader;
        this.classFile = classFile;
        this.data = data;
    }

    public String getClassName() {
        return className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "NewClassData{" +
                "className='" + className + '\'' +
                ", classLoader=" + classLoader +
                ", classFile=" + classFile +
                '}';
    }
}
