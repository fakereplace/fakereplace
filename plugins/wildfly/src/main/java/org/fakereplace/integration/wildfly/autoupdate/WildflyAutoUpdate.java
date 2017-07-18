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

package org.fakereplace.integration.wildfly.autoupdate;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.fakereplace.core.Fakereplace;
import org.fakereplace.replacement.AddedClass;
import org.jboss.modules.ModuleClassLoader;

/**
 * Class that is responsible for handling the wildfly automatic updates. Automatic updates are triggered by web requests,
 *
 * @author Stuart Douglas
 */
public class WildflyAutoUpdate {


    public static final String FAKEREPLACE_SOURCE_PATHS = "fakereplace.source-paths.";


    private static final Map<String, Long> replacedTimestamps = new HashMap<>();
    public static final String DEPLOYMENT = "deployment.";

    private static final Map<ClassLoader, Map<String, byte[]>> REPLACED_CLASSES = new WeakHashMap<>();

    public static synchronized Result runUpdate(ModuleClassLoader classLoader) {

        String moduleName = classLoader.getModule().getIdentifier().getName();
        if (moduleName.startsWith(DEPLOYMENT)) {
            moduleName = moduleName.substring(DEPLOYMENT.length());
        }

        String sourcePaths = System.getProperty(FAKEREPLACE_SOURCE_PATHS + moduleName);
        if (sourcePaths == null) {
            return Result.NO_CHANGE;
        }
        List<Path> paths = Arrays.asList(sourcePaths.split(",")).stream().map((s) -> Paths.get(s)).collect(Collectors.toList());

        try {
            for (Path base : paths) {
                final Map<String, Long> timestamps = new HashMap<>();
                scan(base, base, timestamps);
                List<String> toUpdate = new ArrayList<>();
                List<String> added = new ArrayList<>();
                List<String> replace = new ArrayList<>();
                for (Map.Entry<String, Long> entry : timestamps.entrySet()) {
                    String name = entry.getKey();
                    if (name.endsWith(".java")) {
                        String baseName = name.substring(0, name.length() - 5);
                        Long last = replacedTimestamps.get(baseName);
                        if (last != null) {
                            if (last < entry.getValue()) {
                                toUpdate.add(baseName);
                                replacedTimestamps.put(baseName, entry.getValue());
                                replace.add(baseName);
                            }
                        } else {
                            URL res = classLoader.getResource(baseName + ".class");
                            if (res != null) {
                                URLConnection con = res.openConnection();
                                long lm = con.getLastModified();
                                if (lm < entry.getValue()) {
                                    toUpdate.add(baseName);
                                    replacedTimestamps.put(baseName, entry.getValue());
                                    replace.add(baseName);
                                }
                            } else {
                                toUpdate.add(baseName);
                                replacedTimestamps.put(baseName, entry.getValue());
                                added.add(baseName);
                            }
                        }
                    }
                }
                if (!toUpdate.isEmpty()) {
                    System.out.println("Fakereplace detected the following source files have been changed: " + toUpdate);
                    ClassLoaderCompiler compiler = new ClassLoaderCompiler(classLoader, base, toUpdate);
                    compiler.compile();
                    Map<String, byte[]> byteMap = REPLACED_CLASSES.computeIfAbsent(classLoader, k -> new HashMap<>());
                    AddedClass[] addedClass = new AddedClass[added.size()];
                    for (int i = 0; i < added.size(); ++i) {
                        String className = added.get(i);
                        addedClass[i] = new AddedClass(className, compiler.getOutput().get(className).toByteArray(), classLoader);
                        byteMap.put(className, compiler.getOutput().get(className).toByteArray());
                    }
                    ClassDefinition[] classDefinition = new ClassDefinition[replace.size()];
                    for (int i = 0; i < replace.size(); ++i) {
                        String className = replace.get(i);
                        classDefinition[i] = new ClassDefinition(classLoader.loadClass(className.replace("/", ".")), compiler.getOutput().get(className).toByteArray());
                        byteMap.put(className, compiler.getOutput().get(className).toByteArray());
                    }
                    try {
                        Fakereplace.redefine(classDefinition, addedClass);
                    } catch (Exception e) {
                        System.err.println("Hot replace failed, redeploy required" + e.getMessage());
                        return Result.REDEPLOY_REQUIRED;
                    }
                    return Result.RELOAD;
                }
            }
            return Result.NO_CHANGE;
        } catch (Exception e) {
            System.err.println("Check for updated classes failed");
            e.printStackTrace();
        } finally {
            //something in the compiler clears the TCCL, fix it up
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return Result.NO_CHANGE;
    }

    private static void scan(Path base, Path path, Map<String, Long> timestamps) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach((s) -> scan(base, s, timestamps));
            } else {
                String rel = base.relativize(path).toString();
                timestamps.put(rel, Files.getLastModifiedTime(path).toMillis());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Map<String, byte[]> changedClassDataForLoader(ModuleClassLoader classLoader) {
        return REPLACED_CLASSES.remove(classLoader);
    }

    public enum Result {
        NO_CHANGE,
        RELOAD,
        REDEPLOY_REQUIRED
    }
}
