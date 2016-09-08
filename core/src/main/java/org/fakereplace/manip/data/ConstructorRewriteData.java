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
import org.fakereplace.util.DescriptorUtils;

public class ConstructorRewriteData implements ClassLoaderFiltered<ConstructorRewriteData> {
    private final String clazz;
    private final String methodDesc;
    private final String[] parameters;
    private final int methodNo;
    private final ClassLoader classLoader;

    public ConstructorRewriteData(String clazz, String methodDesc, int methodNo, ClassLoader classLoader) {
        this.clazz = clazz;
        this.methodDesc = methodDesc;
        this.methodNo = methodNo;
        parameters = DescriptorUtils.descriptorStringToParameterArray(methodDesc);
        this.classLoader = classLoader;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(ConstructorRewriteData.class.getName() + " ");
        sb.append(clazz);
        sb.append(" ");
        sb.append(methodDesc);
        sb.append(" ");
        sb.append(methodNo);

        return sb.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getClazz() {
        return clazz;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public String[] getParameters() {
        return parameters;
    }

    public int getMethodNo() {
        return methodNo;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ConstructorRewriteData getInstance() {
        return this;
    }
}
