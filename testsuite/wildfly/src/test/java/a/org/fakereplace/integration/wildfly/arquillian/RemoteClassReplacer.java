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

package a.org.fakereplace.integration.wildfly.arquillian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import a.org.fakereplace.testsuite.shared.HttpUtils;
import javassist.ClassPool;
import javassist.CtClass;

public class RemoteClassReplacer {

    private final Map<String, String> nameReplacements = new HashMap<>();

    private final Map<Class<?>, Class<?>> queuedClassReplacements = new HashMap<>();

    private final Set<Class<?>> addedClasses = new HashSet<Class<?>>();

    private final Map<String, byte[]> replacedResources = new HashMap<>();

    private final ClassPool pool = new ClassPool();

    public RemoteClassReplacer() {
        pool.appendSystemPath();
    }

    public void queueClassForReplacement(Class<?> oldClass, Class<?> newClass) {
        queuedClassReplacements.put(oldClass, newClass);
    }

    public void queueResourceForReplacement(final Class<?> packageClass, final String old, final String newResource) throws IOException {

        final InputStream stream = packageClass.getResource(newResource).openStream();
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            int read;
            byte[] buff = new byte[512];
            while ((read = stream.read(buff)) != -1) {
                bs.write(buff, 0, read);
            }
            replacedResources.put(old, bs.toByteArray());
        } finally {
            stream.close();
        }
    }

    public void addNewClass(Class<?> definition) {
        addedClasses.add(definition);
    }


    public void replaceQueuedClasses(final String deploymentName) {
        try {
            final Map<String, byte[]> classes = new HashMap<>();
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
                classes.put(o.getName(), data);
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeUTF(deploymentName);
            out.writeInt(classes.size());
            for (Map.Entry<String, byte[]> e : classes.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeObject(e.getValue());
            }
            classes.clear();
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
                classes.put(o.getName(), data);
            }
            out.writeInt(addedClasses.size());
            for (Map.Entry<String, byte[]> e : classes.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeObject(e.getValue());
            }
            out.writeInt(replacedResources.size());
            for (Map.Entry<String, byte[]> e : replacedResources.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeObject(e.getValue());
            }
            out.close();

            HttpPost post = new HttpPost("http://localhost:8080/fakereplace/update"); //TODO: should not be hard coded
            post.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes.toByteArray())));
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse result = client.execute(post);
            String body = HttpUtils.getContent(result);
            if (result.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(result.getStatusLine().getStatusCode() + body);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
