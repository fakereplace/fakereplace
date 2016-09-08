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
