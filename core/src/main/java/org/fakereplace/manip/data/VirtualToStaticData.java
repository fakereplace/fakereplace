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

/**
 *
 */
package org.fakereplace.manip.data;

import org.fakereplace.manip.util.ClassLoaderFiltered;

public class VirtualToStaticData implements ClassLoaderFiltered<VirtualToStaticData> {
    private final String oldClass;
    private final String newClass;
    private final String methodName;
    private final String newMethodName;
    private final String methodDesc;
    private final String newStaticMethodDesc;
    private final ClassLoader classLoader;

    public VirtualToStaticData(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, String newMethodName, ClassLoader classLoader) {
        this.oldClass = oldClass;
        this.newClass = newClass;
        this.methodName = methodName;
        if (newMethodName == null) {
            this.newMethodName = methodName;
        } else {
            this.newMethodName = newMethodName;
        }
        this.methodDesc = methodDesc;
        this.newStaticMethodDesc = newStaticMethodDesc;
        this.classLoader = classLoader;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(oldClass);
        sb.append(" ");
        sb.append(newClass);
        sb.append(" ");
        sb.append(methodName);
        sb.append(" ");
        sb.append(methodDesc);
        sb.append(" ");
        sb.append(newStaticMethodDesc);

        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o.getClass().isAssignableFrom(VirtualToStaticData.class)) {
            VirtualToStaticData i = (VirtualToStaticData) o;
            return oldClass.equals(i.oldClass) && newClass.equals(i.newClass) && methodName.equals(i.methodName) && methodDesc.equals(i.methodDesc) && newStaticMethodDesc.equals(i.newStaticMethodDesc);
        }
        return false;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getOldClass() {
        return oldClass;
    }

    public String getNewClass() {
        return newClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getNewMethodName() {
        return newMethodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public String getNewStaticMethodDesc() {
        return newStaticMethodDesc;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public VirtualToStaticData getInstance() {
        return this;
    }
}
