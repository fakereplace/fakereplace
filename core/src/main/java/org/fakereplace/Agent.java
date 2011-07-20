/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace;

import javassist.bytecode.ClassFile;
import org.fakereplace.api.ClassChangeNotifier;
import org.fakereplace.api.IntegrationInfo;
import org.fakereplace.boot.DefaultEnvironment;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.classloading.ClassLookupManager;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.index.UnmodifiedFileIndex;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.replacement.ClassRedefiner;
import org.fakereplace.replacement.ReplacementResult;
import org.fakereplace.server.FakereplaceServer;
import org.fakereplace.transformation.ClassLoaderTransformer;
import org.fakereplace.transformation.MainTransformer;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashSet;
import java.util.Set;

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

        //initialise the unmodified file index
        UnmodifiedFileIndex.loadIndex();

        final Set<IntegrationInfo> integrationInfo = IntegrationLoader.getIntegrationInfo(ClassLoader.getSystemClassLoader());
        inst = i;

        //first we need to instrument the class loaders
        final Set<Class> cls = new HashSet<Class>();
        for (Class c : inst.getAllLoadedClasses()) {
            if (ClassLoader.class.isAssignableFrom(c)) {
                cls.add(c);
            }
        }

        final ClassLoaderTransformer classLoaderTransformer = new ClassLoaderTransformer();
        final MainTransformer mainTransformer = new MainTransformer(integrationInfo);
        Agent.mainTransformer = mainTransformer;
        inst.addTransformer(mainTransformer, true);

        mainTransformer.addTransformer(classLoaderTransformer);

        try {
            inst.retransformClasses(cls.toArray(EMPTY_CL_ARRAY));
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
        mainTransformer.addTransformer(new Transformer(integrationInfo));

        //start the server
        Thread thread = new Thread(new FakereplaceServer(6555));
        thread.setDaemon(true);
        thread.start();
    }

    static public void redefine(ClassDefinition[] classes, AddedClass[] addedData) throws UnmodifiableClassException, ClassNotFoundException {
        final ClassIdentifier[] addedClass = new ClassIdentifier[addedData.length];
        int count = 0;
        for (AddedClass i : addedData) {
            addedClass[count++] = i.getClassIdentifier();
        }

        final Class<?>[] changedClasses = new Class<?>[classes.length];
        count = 0;
        for (ClassDefinition i : classes) {
            System.out.println("Fakereplace is replacing class " + i.getDefinitionClass());
            changedClasses[count++] = i.getDefinitionClass();
            ClassDataStore.markClassReplaced(i.getClass());
        }
        // notify the integration classes that stuff is about to change
        ClassChangeNotifier.beforeChange(changedClasses, addedClass);
        // re-write the classes so their field
        ReplacementResult result = ClassRedefiner.rewriteLoadedClasses(classes);
        try {
            for (AddedClass c : addedData) {
                ClassLookupManager.addClassInfo(c.getClassName(), c.getLoader(), c.getData());
            }
            inst.redefineClasses(result.getClasses());
            if (!result.getClassesToRetransform().isEmpty()) {
                inst.retransformClasses(result.getClassesToRetransform().toArray(new Class[result.getClassesToRetransform().size()]));
            }
            Introspector.flushCaches();

            ClassChangeNotifier.notify(changedClasses, addedClass);
        } catch (Throwable e) {
            try {
                // dump the classes to /tmp so we can look at them
                for (ClassDefinition d : classes) {
                    try {
                        ByteArrayInputStream bin = new ByteArrayInputStream(d.getDefinitionClassFile());
                        DataInputStream dis = new DataInputStream(bin);
                        ClassFile file = new ClassFile(dis);

                        Transformer.getManipulator().transformClass(file, d.getDefinitionClass().getClassLoader(), true);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        file.write(dos);
                        dos.close();

                        String dumpDir = DefaultEnvironment.getEnvironment().getDumpDirectory();
                        if (dumpDir == null) {
                            dumpDir = "/tmp";
                        }
                        FileOutputStream s = new FileOutputStream(dumpDir + '/' + d.getDefinitionClass().getName() + "1.class");
                        dos = new DataOutputStream(s);
                        file.write(dos);
                        dos.flush();
                        dos.close();
                        // s.write(d.getDefinitionClassFile());
                        s.close();
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

}
