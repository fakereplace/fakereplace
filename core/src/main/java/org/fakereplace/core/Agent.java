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

import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import org.fakereplace.api.Extension;
import org.fakereplace.api.NewClassData;
import org.fakereplace.classloading.ClassLookupManager;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataBuilder;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.replacement.AnnotationTransformer;
import org.fakereplace.replacement.FieldReplacementTransformer;
import org.fakereplace.replacement.MethodReplacementTransformer;
import org.fakereplace.server.FakereplaceServer;
import org.fakereplace.transformation.ClassLoaderTransformer;
import org.fakereplace.transformation.FakereplaceTransformer;
import org.fakereplace.transformation.MainTransformer;
import org.fakereplace.transformation.UnmodifiedFileIndex;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The agent entry point.
 *
 * @author stuart
 */
public class Agent {
    private static volatile Instrumentation inst;

    private static volatile MainTransformer mainTransformer;

    /**
     * Entry method for agent
     *
     * @param args            args given to agent
     * @param instrumentation runtime instrumentation instance
     */
    public static void premain(String args, Instrumentation instrumentation) {
        AgentOptions.setup(args);
        inst = instrumentation;

        //initialise the unmodified file index
        UnmodifiedFileIndex.loadIndex();

        final Set<Extension> extensions = getIntegrationInfo(ClassLoader.getSystemClassLoader());

        initMainTransformer(extensions, new ClassLoaderTransformer(),
                new AnnotationTransformer(),
                new FieldReplacementTransformer(),
                new MethodReplacementTransformer(),
                new Transformer(extensions));

        FakereplaceServer.startFakereplaceServerDaemonThread(AgentOptions.getOption(AgentOption.SERVER));
    }

    private static void initMainTransformer(Set<Extension> extensions,
                                            ClassLoaderTransformer classLoaderTransformer,
                                            FakereplaceTransformer... transformers) {
        final MainTransformer mainTransformer = new MainTransformer(extensions);
        Agent.mainTransformer = mainTransformer;
        inst.addTransformer(mainTransformer, true);

        mainTransformer.addTransformer(classLoaderTransformer);

        instrumentClassloaders();

        Arrays.stream(transformers).forEach(mainTransformer::addTransformer);

        mainTransformer.setRetransformationStarted(false);
        mainTransformer.setLogClassRetransformation(true);
    }

    private static void instrumentClassloaders() {
        //first we need to instrument the class loaders
        final Set<Class> allClasses = Arrays.stream(inst.getAllLoadedClasses())
                .filter(ClassLoader.class::isAssignableFrom)
                .collect(Collectors.toSet());

        try {
            inst.retransformClasses(allClasses.toArray(new Class[allClasses.size()]));
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }

    public static void redefine(ClassDefinition[] toRedefineClasses, AddedClass[] addedData) throws UnmodifiableClassException, ClassNotFoundException {
        redefine(toRedefineClasses, addedData, true);
    }

    public static void redefine(ClassDefinition[] classesToRedefine, AddedClass[] addedClasses, boolean wait) throws UnmodifiableClassException, ClassNotFoundException {
        try {
            for (AddedClass addedClass : addedClasses) {
                ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(addedClass.getData())));
                mainTransformer.addNewClass(new NewClassData(addedClass.getClassName(), addedClass.getLoader(), cf));
            }
            for (ClassDefinition i : classesToRedefine) {
                ClassDataStore.instance().markClassReplaced(i.getDefinitionClass());
                BaseClassData baseClassData = ClassDataStore.instance().getBaseClassData(i.getDefinitionClass().getClassLoader(), i.getDefinitionClass().getName());
                if (baseClassData != null) {
                    ClassDataStore.instance().saveClassData(i.getDefinitionClass().getClassLoader(), i.getDefinitionClass().getName(), new ClassDataBuilder(baseClassData));
                }
            }
            // re-write the classes so their field
            for (AddedClass addedClass : addedClasses) {
                ClassLookupManager.addClassInfo(addedClass.getClassName(), addedClass.getLoader(), addedClass.getData());
            }
            inst.redefineClasses(classesToRedefine);
            Introspector.flushCaches();
            if (wait) {
                mainTransformer.waitForTasks();
            }
        } catch (Throwable e) {
            try {
                dumpClassesToTemp(classesToRedefine);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }

    private static void dumpClassesToTemp(ClassDefinition[] classes) throws BadBytecode {
        // dump the classes to /tmp so we can look at them
        for (ClassDefinition clazz : classes) {
            try {
                ByteArrayInputStream bin = new ByteArrayInputStream(clazz.getDefinitionClassFile());
                DataInputStream dis = new DataInputStream(bin);
                final ClassFile file = new ClassFile(dis);
                Transformer.getManipulator().transformClass(file, clazz.getDefinitionClass().getClassLoader(), true, new HashSet<>());
                String dumpDir = AgentOptions.getOption(AgentOption.DUMP_DIR);
                if (dumpDir != null) {
                    FileOutputStream fos = new FileOutputStream(dumpDir + '/' + clazz.getDefinitionClass().getName() + "1.class");
                    DataOutputStream dos = new DataOutputStream(fos);
                    file.write(dos);
                    dos.flush();
                    dos.close();
                    fos.close();
                }
            } catch (IOException a) {
                a.printStackTrace();
            }
        }
    }

    public static Instrumentation getInstrumentation() {
        return inst;
    }

    private static Set<Extension> getIntegrationInfo(ClassLoader clr) {
        final ServiceLoader<Extension> extensionLoader = ServiceLoader.load(Extension.class, clr);
        final Set<Extension> integrations = new HashSet<>();
        for (Extension extension : extensionLoader) {
            integrations.add(extension);
        }
        return integrations;
    }

    public static boolean isRetransformationStarted() {
        return mainTransformer.isRetransformationStarted();
    }
}
