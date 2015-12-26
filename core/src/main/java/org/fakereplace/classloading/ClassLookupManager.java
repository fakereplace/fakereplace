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

package org.fakereplace.classloading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.core.Constants;
import org.fakereplace.transformation.MainTransformer;

/**
 * this class is resposible for serving up classes to instrumented ClassLoaders
 *
 * @author stuart
 */
public class ClassLookupManager {
    private static final Map<ClassIdentifier, byte[]> classData = new ConcurrentHashMap<>();

    public static byte[] getClassData(String className, Object cl) {
        //if is possible for this to be called by an object that is not a CL
        //do nothing in this case
        if (!(cl instanceof ClassLoader)) {
            return null;
        }
        final ClassLoader loader = (ClassLoader) cl;
        if (className.startsWith(Constants.GENERATED_CLASS_PACKAGE)) {
            return ProxyDefinitionStore.getProxyDefinition(loader, className);
        }
        if (className.startsWith("org.fakereplace.integration")) {
            return MainTransformer.getIntegrationClass(loader, className);
        }
        return classData.get(new ClassIdentifier(className, loader));
    }

    public static void addClassInfo(String className, ClassLoader loader, byte[] data) {
        classData.put(new ClassIdentifier(className, loader), data);
    }
}
