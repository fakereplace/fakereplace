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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fakereplace.client.ClassData;
import org.fakereplace.client.ContentSource;
import org.fakereplace.client.FakeReplaceClient;
import org.fakereplace.client.ResourceData;
import javassist.ClassPool;
import javassist.CtClass;

public class RemoteClassReplacer {

    private final Map<String, String> nameReplacements = new HashMap<String, String>();

    private final Map<Class<?>, Class<?>> queuedClassReplacements = new HashMap<Class<?>, Class<?>>();

    private final Set<Class<?>> addedClasses = new HashSet<Class<?>>();

    private final Map<String, ResourceData> replacedResources = new HashMap<String, ResourceData>();

    private final ClassPool pool = new ClassPool();

    public RemoteClassReplacer() {
        pool.appendSystemPath();
    }

    public void queueClassForReplacement(Class<?> oldClass, Class<?> newClass) {
        queuedClassReplacements.put(oldClass, newClass);
    }

    public void queueResourceForReplacement(final Class<?> packageClass, final String old, final String newResource) {
        replacedResources.put(old, new ResourceData(old, new Date().getTime(), new ContentSource() {
            @Override
            public byte[] getData() throws IOException {
                final InputStream stream = packageClass.getResource(newResource).openStream();
                try {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    int read;
                    byte[] buff = new byte[512];
                    while ((read = stream.read(buff)) != -1) {
                        bs.write(buff, 0, read);
                    }
                    return bs.toByteArray();
                } finally {
                    stream.close();
                }

            }
        }));
    }

    public void addNewClass(Class<?> definition) {
        addedClasses.add(definition);
    }


    public void replaceQueuedClasses(final String deploymentName) {
        try {
            final Map<String, ClassData> classes = new HashMap<String, ClassData>();
            for (Class<?> o : queuedClassReplacements.keySet()) {
                Class<?> n = queuedClassReplacements.get(o);
                String newName = o.getName();
                String oldName = n.getName();
                nameReplacements.put(oldName, newName);
            }

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
                final byte[] data = nc.toBytecode();
                classes.put(o.getName(), new ClassData(o.getName(), System.currentTimeMillis() + 100, () -> data));
            }
            for (Class<?> o : addedClasses) {
                CtClass nc = pool.get(o.getName());

                if (nc.isFrozen()) {
                    nc.defrost();
                }

                for (String newName : nameReplacements.keySet()) {
                    String oldName = nameReplacements.get(newName);
                    nc.replaceClassName(newName, oldName);
                }
                final byte[] data = nc.toBytecode();
                classes.put(o.getName(), new ClassData(o.getName(), System.currentTimeMillis() + 100, () -> data));
            }
            FakeReplaceClient.run(deploymentName, classes, replacedResources);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
