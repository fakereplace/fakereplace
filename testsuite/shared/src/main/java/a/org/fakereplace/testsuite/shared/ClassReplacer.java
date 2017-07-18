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

package a.org.fakereplace.testsuite.shared;

import java.lang.instrument.ClassDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fakereplace.core.Fakereplace;
import org.fakereplace.replacement.AddedClass;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

public class ClassReplacer {

    private final Map<String, String> nameReplacements = new HashMap<>();

    private final Map<Class<?>, Class<?>> queuedClassReplacements = new HashMap<>();

    private final Map<Class<?>, String> addedClasses = new HashMap<>();

    private final ClassPool pool = new ClassPool();

    public ClassReplacer() {
        pool.appendClassPath(new LoaderClassPath(ClassReplacer.class.getClassLoader()));
    }

    public void queueClassForReplacement(Class<?> oldClass, Class<?> newClass) {
        queuedClassReplacements.put(oldClass, newClass);
    }

    public void addNewClass(Class<?> definition, String name) {
        addedClasses.put(definition, name);
    }

    public void replaceQueuedClasses() {
        replaceQueuedClasses(true);
    }

    public void replaceQueuedClassesWithInstrumentation() {
        replaceQueuedClasses(false);
    }

    public void replaceQueuedClasses(boolean useFakereplace) {
        try {
            ClassDefinition[] definitions = new ClassDefinition[queuedClassReplacements.size()];
            AddedClass[] newClasses = new AddedClass[addedClasses.size()];
            for (Class<?> o : queuedClassReplacements.keySet()) {
                Class<?> n = queuedClassReplacements.get(o);
                String newName = o.getName();
                String oldName = n.getName();
                nameReplacements.put(oldName, newName);
            }

            for (Entry<Class<?>, String> o : addedClasses.entrySet()) {
                nameReplacements.put(o.getKey().getName(), o.getValue());
            }
            int count = 0;
            for (Class<?> o : queuedClassReplacements.keySet()) {
                Class<?> n = queuedClassReplacements.get(o);
                CtClass nc = pool.get(n.getName());

                if (nc.isFrozen()) {
                    nc.defrost();
                }

                for (String oldName : nameReplacements.keySet()) {
                    String newName = nameReplacements.get(oldName);
                    nc.replaceClassName(oldName, newName);
                }
                nc.setName(o.getName());
                ClassDefinition cd = new ClassDefinition(o, nc.toBytecode());
                definitions[count++] = cd;
            }
            count = 0;
            for (Entry<Class<?>, String> o : addedClasses.entrySet()) {
                CtClass nc = pool.get(o.getKey().getName());

                if (nc.isFrozen()) {
                    nc.defrost();
                }

                for (String newName : nameReplacements.keySet()) {
                    String oldName = nameReplacements.get(newName);
                    nc.replaceClassName(newName, oldName);
                }
                AddedClass ncd = new AddedClass(o.getValue(), nc.toBytecode(), o.getKey().getClassLoader());
                newClasses[count++] = ncd;
            }

            if (useFakereplace) {
                Fakereplace.redefine(definitions, newClasses);
            } else {
                Fakereplace.getInstrumentation().redefineClasses(definitions);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
