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

package org.fakereplace.manip.data;

import org.fakereplace.manip.util.ClassLoaderFiltered;

public class StaticFieldAccessRewriteData implements ClassLoaderFiltered<StaticFieldAccessRewriteData> {
    private final String oldClass;
    private final String newClass;
    private final String fieldName;
    private final ClassLoader classLoader;

    public StaticFieldAccessRewriteData(String oldClass, String newClass, String fieldName, ClassLoader classLoader) {
        this.oldClass = oldClass;
        this.newClass = newClass;
        this.fieldName = fieldName;
        this.classLoader = classLoader;
    }

    public String getOldClass() {
        return oldClass;
    }

    public String getNewClass() {
        return newClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(oldClass);
        sb.append(" ");
        sb.append(newClass);
        sb.append(" ");
        sb.append(fieldName);

        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o.getClass().isAssignableFrom(StaticFieldAccessRewriteData.class)) {
            StaticFieldAccessRewriteData i = (StaticFieldAccessRewriteData) o;
            return oldClass.equals(i.oldClass) && newClass.equals(i.newClass) && fieldName.equals(i.fieldName);
        }
        return false;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public StaticFieldAccessRewriteData getInstance() {
        return this;
    }
}
