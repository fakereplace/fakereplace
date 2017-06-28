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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.api.environment.ChangedClasses;
import org.fakereplace.api.environment.Environment;
import org.fakereplace.logging.Logger;

/**
 * class that stores some basic environment info.
 *
 * @author stuart
 */
public class DefaultEnvironment implements Environment {

    private static final Logger log = Logger.getLogger(DefaultEnvironment.class);

    private static final String[] replaceablePackages;

    private final Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();
    private final Map<String, ClassLoader> loaders = new ConcurrentHashMap<String, ClassLoader>();

    public static final DefaultEnvironment INSTANCE = new DefaultEnvironment();

    static {
        String plist = AgentOptions.getOption(AgentOption.PACKAGES);
        if (plist == null || plist.length() == 0) {
            replaceablePackages = new String[0];
        } else {
            replaceablePackages = plist.split(";");
        }
    }

    @Override
    public boolean isClassReplaceable(String className, ClassLoader loader) {
        if(className == null) {
            return false; //lambdas
        }
        if(loader == null) {
            return false;
        }
        className = className.replace("/", ".");
        for (String i : replaceablePackages) {
            if (className.startsWith(i)) {
                log.trace(className + " is replaceable as it belongs to " + i);
                return true;
            }
        }
        if (className.contains("$Proxy")) {
            log.trace(className + " is replaceable as it is a proxy");
            return true;
        }
        URL u = loader.getResource(className.replace('.', '/') + ".class");
        if (u != null) {
            if (u.getProtocol().equals("file") || u.getProtocol().equals("vfsfile")) {
                log.trace(className + " is replaceable as it is exploded");
                return true;
            }
        }

        log.trace(className + " is not replaceable");
        return false;
    }

    public void recordTimestamp(String className, ClassLoader loader) {
        log.trace("Recording timestamp for " + className);
        if (loader == null) {
            return;
        }
        final URL file = loader.getResource(className.replace(".", "/") + ".class");
        className = className.replace("/", ".");
        if (file != null) {
            URLConnection connection = null;
            try {
                connection = file.openConnection();
                timestamps.put(className, connection.getLastModified());
                loaders.put(className, loader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public ChangedClasses getUpdatedClasses(final String deploymentName, Map<String, Long> updatedClasses) {
        final Set<Class<?>> ret = new HashSet<Class<?>>();
        ClassLoader loader = null;
        for (Map.Entry<String, Long> entry : updatedClasses.entrySet()) {
            if (timestamps.containsKey(entry.getKey()) && timestamps.get(entry.getKey()) < entry.getValue()) {
                loader = loaders.get(entry.getKey());
                try {
                    ret.add(loader.loadClass(entry.getKey()));
                    timestamps.put(entry.getKey(), entry.getValue());
                } catch (ClassNotFoundException e) {
                    log.error("Could not load class " + entry, e);
                }
            }
        }
        return new ChangedClasses(ret, Collections.<String>emptySet(), loader);
    }

    @Override
    public Set<String> getUpdatedResources(final String deploymentName, final Map<String, Long> updatedResources) {
        return Collections.emptySet();
    }

    @Override
    public void updateResource(final String archiveName, final Map<String, byte[]> replacedResources) {

    }
}
