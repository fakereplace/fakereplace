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

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fakereplace.Extension;
import org.fakereplace.ReplaceableClassSelector;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataBuilder;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.replacement.AnnotationTransformer;
import org.fakereplace.replacement.FieldReplacementTransformer;
import org.fakereplace.replacement.MethodReplacementTransformer;

import javassist.bytecode.ClassFile;

/**
 * The agent entry point.
 *
 * @author stuart
 */
public class Fakereplace {

    private static final Class[] EMPTY_CL_ARRAY = new Class[0];

    private static volatile Instrumentation inst;

    private static volatile MainTransformer mainTransformer;

    private static final List<ReplaceableClassSelector> replaceableClassSelectors = new CopyOnWriteArrayList<>();

    private static final List<ClassChangeAware> classChangeAwares = new CopyOnWriteArrayList<>();


    public static void premain(java.lang.String s, java.lang.instrument.Instrumentation i) {

        AgentOptions.setup(s);
        inst = i;

        final Set<Extension> extension = getIntegrationInfo(ClassLoader.getSystemClassLoader());

        replaceableClassSelectors.add(DefaultReplaceableClassSelector.INSTANCE);

        //first we need to instrument the class loaders
        final Set<Class> cls = new HashSet<>();
        for (Class c : inst.getAllLoadedClasses()) {
            if (ClassLoader.class.isAssignableFrom(c)) {
                cls.add(c);
            }
        }

        final ClassLoaderTransformer classLoaderTransformer = new ClassLoaderTransformer();
        final MainTransformer mainTransformer = new MainTransformer();
        Fakereplace.mainTransformer = mainTransformer;
        inst.addTransformer(mainTransformer, true);

        mainTransformer.addTransformer(classLoaderTransformer);

        try {
            inst.retransformClasses(cls.toArray(EMPTY_CL_ARRAY));
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
        mainTransformer.addTransformer(new IntegrationActivationTransformer(extension));
        mainTransformer.addTransformer(new AnnotationTransformer());
        mainTransformer.addTransformer(new FieldReplacementTransformer());
        mainTransformer.addTransformer(new MethodReplacementTransformer());
        mainTransformer.addTransformer(new Transformer());
        mainTransformer.addTransformer(new ClassInfoTransformer());
        mainTransformer.setRetransformationStarted(false);
        mainTransformer.setLogClassRetransformation(true);
    }

    public static void redefine(ClassDefinition[] classes, AddedClass[] addedData) {
        redefine(classes, addedData, true);
    }

    public static void redefine(ClassDefinition[] classes, AddedClass[] addedData, boolean wait) {
        try {
            for (AddedClass i : addedData) {
                ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(i.getData())));
                mainTransformer.addNewClass(new NewClassData(i.getClassName(), i.getLoader(), cf, i.getData()));
            }
            for (ClassDefinition i : classes) {
                ClassDataStore.instance().markClassReplaced(i.getDefinitionClass());
                BaseClassData baseClassData = ClassDataStore.instance().getBaseClassData(i.getDefinitionClass().getClassLoader(), i.getDefinitionClass().getName());
                if (baseClassData != null) {
                    ClassDataStore.instance().saveClassData(i.getDefinitionClass().getClassLoader(), i.getDefinitionClass().getName(), new ClassDataBuilder(baseClassData));
                }
            }
            for (AddedClass c : addedData) {
                ClassLookupManager.addClassInfo(c.getClassName(), c.getLoader(), c.getData());
            }
            inst.redefineClasses(classes);
            clearJvmCaches();
            if (wait) {
                mainTransformer.waitForTasks();
            }
        } catch (Throwable e) {
            try {
                // dump the classes to /tmp so we can look at them
                for (ClassDefinition d : classes) {
                    try {
                        ByteArrayInputStream bin = new ByteArrayInputStream(d.getDefinitionClassFile());
                        DataInputStream dis = new DataInputStream(bin);
                        final ClassFile file = new ClassFile(dis);
                        Transformer.getManipulator().transformClass(file, d.getDefinitionClass().getClassLoader(), true, new HashSet<>(), Fakereplace.isClassReplaceable(d.getDefinitionClass().getName(), d.getDefinitionClass().getClassLoader()));
                        String dumpDir = AgentOptions.getOption(AgentOption.DUMP_DIR);
                        if (dumpDir != null) {
                            FileOutputStream s = new FileOutputStream(dumpDir + '/' + d.getDefinitionClass().getName() + "1.class");
                            DataOutputStream dos = new DataOutputStream(s);
                            file.write(dos);
                            dos.flush();
                            dos.close();
                            // s.write(d.getDefinitionClassFile());
                            s.close();
                        }
                    } catch (IOException a) {
                        a.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            throw (new RuntimeException(e));
        }
    }

    private static void clearJvmCaches() {
        Introspector.flushCaches();
        ClassInfoTransformer.clearClassInfoCache();
    }

    public static Instrumentation getInstrumentation() {
        return inst;
    }

    private static Set<Extension> getIntegrationInfo(ClassLoader clr) {
        final ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class, clr);
        final Set<Extension> integrations = new HashSet<>();
        for (Extension aLoader : loader) {
            integrations.add(aLoader);
        }
        return integrations;
    }

    public static boolean isRetransformationStarted() {
        return mainTransformer.isRetransformationStarted();
    }

    public static boolean isClassReplaceable(String className, ClassLoader classLoader) {
        for (ReplaceableClassSelector env : replaceableClassSelectors) {
            if (env.isClassReplaceable(className, classLoader)) {
                return true;
            }
        }
        return false;
    }

    public static void addReplaceableClassSelector(ReplaceableClassSelector replaceableClassSelector) {
        replaceableClassSelectors.add(replaceableClassSelector);
    }
    public static void removeReplaceableClassSelector(ReplaceableClassSelector replaceableClassSelector) {
        replaceableClassSelectors.remove(replaceableClassSelector);
    }

    public static void addClassChangeAware(ClassChangeAware classChangeAware) {
        classChangeAwares.add(classChangeAware);
    }

    public static void removeClassChangeAware(ClassChangeAware classChangeAware) {
        classChangeAwares.remove(classChangeAware);
    }

    static List<ClassChangeAware> getClassChangeAwares() {
        return classChangeAwares;
    }
}
