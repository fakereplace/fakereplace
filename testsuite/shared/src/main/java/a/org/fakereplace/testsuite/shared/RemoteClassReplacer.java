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

package a.org.fakereplace.testsuite.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javassist.ClassPool;
import javassist.CtClass;
import org.fakereplace.client.ClassData;
import org.fakereplace.client.ContentSource;
import org.fakereplace.client.FakeReplaceClient;
import org.fakereplace.client.ResourceData;

public class RemoteClassReplacer {

    private final Map<String, String> nameReplacements = new HashMap<String, String>();

    private final Map<Class<?>, Class<?>> queuedClassReplacements = new HashMap<Class<?>, Class<?>>();

    private final Map<Class<?>, String> addedClasses = new HashMap<Class<?>, String>();

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

    public void addNewClass(Class<?> definition, String name) {
        addedClasses.put(definition, name);
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

            for (Entry<Class<?>, String> o : addedClasses.entrySet()) {
                nameReplacements.put(o.getKey().getName(), o.getValue());
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
                classes.put(o.getName(), new ClassData(o.getName(), new Date().getTime(), new ContentSource() {
                    @Override
                    public byte[] getData() throws IOException {
                        return data;
                    }
                }));
            }
            for (Entry<Class<?>, String> o : addedClasses.entrySet()) {
                CtClass nc = pool.get(o.getKey().getName());

                if (nc.isFrozen()) {
                    nc.defrost();
                }

                for (String newName : nameReplacements.keySet()) {
                    String oldName = nameReplacements.get(newName);
                    nc.replaceClassName(newName, oldName);
                }
                final byte[] data = nc.toBytecode();
                classes.put(o.getKey().getName(), new ClassData(o.getKey().getName(), new Date().getTime(), new ContentSource() {
                    @Override
                    public byte[] getData() throws IOException {
                        return data;
                    }
                }));
            }
            FakeReplaceClient.run(deploymentName, classes, replacedResources);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
