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
package org.fakereplace.transformation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.Extension;
import org.fakereplace.api.environment.CurrentEnvironment;
import org.fakereplace.api.environment.Environment;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.core.AgentOption;
import org.fakereplace.core.AgentOptions;
import org.fakereplace.core.ClassChangeNotifier;
import org.fakereplace.core.DefaultEnvironment;
import org.fakereplace.logging.Logger;

/**
 * @author Stuart Douglas
 */
public class MainTransformer implements ClassFileTransformer {

    private volatile FakereplaceTransformer[] transformers = {};

    private final Map<String, Extension> integrationClassTriggers;

    private final Set<String> loadedClassChangeAwares = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private static final Set<ClassLoader> integrationClassloader = Collections.newSetFromMap(new MapMaker().weakKeys().<ClassLoader, Boolean>makeMap());

    public MainTransformer(Set<Extension> extension) {
        Map<String, Extension> integrationClassTriggers = new HashMap<String, Extension>();
        for (Extension i : extension) {
            for (String j : i.getIntegrationTriggerClassNames()) {
                integrationClassTriggers.put(j.replace(".", "/"), i);
            }
        }
        this.integrationClassTriggers = integrationClassTriggers;
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

        final Environment environment = CurrentEnvironment.getEnvironment();
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
                        final Class<?> envClass = Class.forName(newEnv, true, loader);
                        final Environment newEnvironment = (Environment) envClass.newInstance();
                        if (environment instanceof DefaultEnvironment) {
                            CurrentEnvironment.setEnvironment(newEnvironment);
                        } else {
                            Logger.getLogger(MainTransformer.class).error("Could not set environment to " + newEnvironment + " it has already been changed to " + environment);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        boolean changed = false;
        if (UnmodifiedFileIndex.isClassUnmodified(className)) {
            return null;
        }

        final ClassFile file;
        try {
            file = new ClassFile(new DataInputStream(new ByteArrayInputStream(classfileBuffer)));
            for (final FakereplaceTransformer transformer : transformers) {
                if (transformer.transform(loader, className, classBeingRedefined, protectionDomain, file)) {
                    changed = true;
                }
            }
            if (!changed) {
                UnmodifiedFileIndex.markClassUnmodified(className);
                return null;
            } else {
                try {

                    ClassPool classPool = new ClassPool();
                    classPool.appendClassPath(new LoaderClassPath(loader));
                    classPool.appendSystemPath();
                    for (MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                        method.rebuildStackMap(classPool);
                    }
                } catch (BadBytecode e) {
                    throw new RuntimeException(e);
                }
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                file.write(new DataOutputStream(bs));
                // dump the class for debugging purposes
                final String dumpDir = AgentOptions.getOption(AgentOption.DUMP_DIR);
                if (dumpDir != null) {
                    FileOutputStream s = new FileOutputStream(dumpDir + '/' + file.getName() + ".class");
                    DataOutputStream dos = new DataOutputStream(s);
                    file.write(dos);
                    s.close();
                }
                return bs.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalClassFormatException(e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public synchronized void addTransformer(FakereplaceTransformer transformer) {
        final FakereplaceTransformer[] transformers = new FakereplaceTransformer[this.transformers.length + 1];
        for (int i = 0; i < this.transformers.length; ++i) {
            transformers[i] = this.transformers[i];
        }
        transformers[this.transformers.length] = transformer;
        this.transformers = transformers;
    }

    public synchronized void removeTransformer(FakereplaceTransformer transformer) {
        final FakereplaceTransformer[] transformers = new FakereplaceTransformer[this.transformers.length - 1];
        int j = 0;
        for (int i = 0; i < this.transformers.length; ++i) {
            FakereplaceTransformer value = this.transformers[i];
            if (value != transformer) {
                transformers[++j] = this.transformers[i];
            }
        }
        this.transformers = transformers;
    }


    public static byte[] getIntegrationClass(ClassLoader c, String name) {
        if (!integrationClassloader.contains(c)) {
            return null;
        }
        URL resource = ClassLoader.getSystemClassLoader().getResource(name.replace('.', '/') + ".class");
        InputStream in = null;
        try {
            in = resource.openStream();
            return org.fakereplace.util.FileReader.readFileBytes(resource.openStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
