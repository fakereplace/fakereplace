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
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

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
import org.fakereplace.transformation.MainTransformer;
import org.fakereplace.transformation.UnmodifiedFileIndex;
import javassist.bytecode.ClassFile;

/**
 * The agent entry point.
 *
 * @author stuart
 */
public class Agent {

    private static final Class[] EMPTY_CL_ARRAY = new Class[0];

    private static volatile Instrumentation inst;

    private static volatile MainTransformer mainTransformer;


    public static void premain(java.lang.String s, java.lang.instrument.Instrumentation i) {

        AgentOptions.setup(s);
        inst = i;

        final Set<Extension> extension = getIntegrationInfo(ClassLoader.getSystemClassLoader());

        //initialise the unmodified file index
        UnmodifiedFileIndex.loadIndex();

        //first we need to instrument the class loaders
        final Set<Class> cls = new HashSet<Class>();
        for (Class c : inst.getAllLoadedClasses()) {
            if (ClassLoader.class.isAssignableFrom(c)) {
                cls.add(c);
            }
        }

        final ClassLoaderTransformer classLoaderTransformer = new ClassLoaderTransformer();
        final MainTransformer mainTransformer = new MainTransformer(extension);
        Agent.mainTransformer = mainTransformer;
        inst.addTransformer(mainTransformer, true);

        mainTransformer.addTransformer(classLoaderTransformer);

        try {
            inst.retransformClasses(cls.toArray(EMPTY_CL_ARRAY));
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
        mainTransformer.addTransformer(new AnnotationTransformer());
        mainTransformer.addTransformer(new FieldReplacementTransformer());
        mainTransformer.addTransformer(new MethodReplacementTransformer());
        mainTransformer.addTransformer(new Transformer(extension));

        //start the server
        Thread thread = new Thread(new FakereplaceServer(Integer.parseInt(AgentOptions.getOption(AgentOption.PORT))));
        thread.setDaemon(true);
        thread.setName("Fakereplace Thread");
        thread.start();
    }

    public static void redefine(ClassDefinition[] classes, AddedClass[] addedData) throws UnmodifiableClassException, ClassNotFoundException {
        try {
            for (AddedClass i : addedData) {
                ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(i.getData())));
                mainTransformer.addNewClass(new NewClassData(i.getClassName(), i.getLoader(), cf));
            }
            for (ClassDefinition i : classes) {
                System.out.println("Fakereplace is replacing class " + i.getDefinitionClass());
                ClassDataStore.instance().markClassReplaced(i.getDefinitionClass());
                BaseClassData baseClassData = ClassDataStore.instance().getBaseClassData(i.getDefinitionClass().getClassLoader(), i.getDefinitionClass().getName());
                if (baseClassData != null) {
                    ClassDataStore.instance().saveClassData(i.getDefinitionClass().getClassLoader(), i.getDefinitionClass().getName(), new ClassDataBuilder(baseClassData));
                }
            }
            // re-write the classes so their field
            for (AddedClass c : addedData) {
                ClassLookupManager.addClassInfo(c.getClassName(), c.getLoader(), c.getData());
            }
            inst.redefineClasses(classes);
            Introspector.flushCaches();
            mainTransformer.runIntegration();
        } catch (Throwable e) {
            try {
                // dump the classes to /tmp so we can look at them
                for (ClassDefinition d : classes) {
                    try {
                        ByteArrayInputStream bin = new ByteArrayInputStream(d.getDefinitionClassFile());
                        DataInputStream dis = new DataInputStream(bin);
                        final ClassFile file = new ClassFile(dis);
                        Transformer.getManipulator().transformClass(file, d.getDefinitionClass().getClassLoader(), true);
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

    public static Instrumentation getInstrumentation() {
        return inst;
    }

    public static Set<Extension> getIntegrationInfo(ClassLoader clr) {
        final ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class, clr);
        final Set<Extension> integrations = new HashSet<Extension>();
        final Iterator<Extension> it = loader.iterator();
        while (it.hasNext()) {
            integrations.add(it.next());
        }
        return integrations;
    }
}
