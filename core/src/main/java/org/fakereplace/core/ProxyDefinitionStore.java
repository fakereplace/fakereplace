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

package org.fakereplace.core;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.fakereplace.data.ClassLoaderData;

/**
 * This class holds proxy definitions, that are later loaded by the relevant ClassLoaders
 *
 * @author stuart
 */
public class ProxyDefinitionStore {
    private static final AtomicLong proxyNo = new AtomicLong();

    public static byte[] getProxyDefinition(ClassLoader classLoader, String name) {
        Map<String, byte[]> def = ClassLoaderData.get(classLoader).getProxyDefinitions();
        return def.get(name);
    }

    public static void saveProxyDefinition(ClassLoader classLoader, String className, byte[] data) {
        Map<String, byte[]> def = ClassLoaderData.get(classLoader).getProxyDefinitions();
        def.put(className, data);
    }

    /**
     * Returns a unique proxy name
     */
    public static String getProxyName() {
        return Constants.GENERATED_CLASS_PACKAGE + ".ProxyClass" + proxyNo.incrementAndGet();
    }

}
