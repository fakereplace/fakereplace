/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
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
