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

import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.Extension;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.environment.CurrentEnvironment;
import org.fakereplace.api.environment.Environment;
import org.fakereplace.logging.Logger;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;

/**
 * @author Stuart Douglas
 */
public class IntegrationActivationTransformer implements FakereplaceTransformer {

    private final Map<String, Extension> integrationClassTriggers;

    private final Set<String> loadedClassChangeAwares = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private static final Set<ClassLoader> integrationClassloader = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));


    public IntegrationActivationTransformer(Set<Extension> extension) {
        Map<String, Extension> integrationClassTriggers = new HashMap<>();
        for (Extension i : extension) {
            for (String j : i.getIntegrationTriggerClassNames()) {
                integrationClassTriggers.put(j.replace(".", "/"), i);
            }
        }
        this.integrationClassTriggers = integrationClassTriggers;
    }

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {

        if (integrationClassTriggers.containsKey(className)) {
            integrationClassloader.add(loader);
            // we need to load the class in another thread
            // otherwise it will not go through the javaagent
            final Extension extension = integrationClassTriggers.get(className);
            if (!loadedClassChangeAwares.contains(extension.getClassChangeAwareName())) {
                loadedClassChangeAwares.add(extension.getClassChangeAwareName());
                try {
                    Class<?> clazz = Class.forName(extension.getClassChangeAwareName(), true, loader);
                    final Object intance = clazz.newInstance();
                    if (intance instanceof ClassChangeAware) {
                        ClassChangeNotifier.instance().add((ClassChangeAware) intance);
                    }
                    final String newEnv = extension.getEnvironment();
                    if (newEnv != null) {
                        final Environment environment = CurrentEnvironment.getEnvironment();
                        final Class<?> envClass = Class.forName(newEnv, true, loader);
                        final Environment newEnvironment = (Environment) envClass.newInstance();
                        if (environment instanceof DefaultEnvironment) {
                            CurrentEnvironment.setEnvironment(newEnvironment);
                        } else {
                            Logger.getLogger(MainTransformer.class).error("Could not set environment to " + newEnvironment + " it has already been changed to " + environment);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static byte[] getIntegrationClass(ClassLoader c, String name) {
        if (!integrationClassloader.contains(c)) {
            return null;
        }
        URL resource = ClassLoader.getSystemClassLoader().getResource(name.replace('.', '/') + ".class");
        if (resource == null) {
            throw new RuntimeException("Could not load integration class " + name);
        }
        try (InputStream in = resource.openStream()) {
            return org.fakereplace.util.FileReader.readFileBytes(resource.openStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
