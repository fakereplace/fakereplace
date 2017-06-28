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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.data.ClassLoaderData;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.util.FileReader;
import org.fakereplace.util.MD5;
import org.fakereplace.util.WatchServiceFileSystemWatcher;
import javassist.bytecode.ClassFile;


/**
 * Class that is responsible for watching the file system and reporting on class change events.
 * <p>
 * Internally it uses {@link org.fakereplace.util.WatchServiceFileSystemWatcher} to watch the file system.
 *
 * @author Stuart Douglas
 */
class FileSystemWatcher {

    private final WatchServiceFileSystemWatcher watcher = new WatchServiceFileSystemWatcher();

    private final Set<File> registered = new HashSet<>();

    private final Map<String, String> hashes = new HashMap<>();

    private final ClassLoaderData.AttachmentKey<Callback> callbackAttachmentKey = new ClassLoaderData.AttachmentKey<>();

    private final class Callback implements WatchServiceFileSystemWatcher.FileChangeCallback  {

        private final ClassLoader classLoader;

        private Callback(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public void handleChanges(Collection<WatchServiceFileSystemWatcher.FileChangeEvent> changes) {
            try {
                List<AddedClass> addedClasses = new ArrayList<>();
                List<ClassDefinition> changedClasses = new ArrayList<>();
                for (WatchServiceFileSystemWatcher.FileChangeEvent change : changes) {
                    if (change.getType() == WatchServiceFileSystemWatcher.FileChangeEvent.Type.ADDED) {
                        try (FileInputStream in = new FileInputStream(change.getFile())) {
                            byte[] bytes = FileReader.readFileBytes(in);
                            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(bytes)));
                            addedClasses.add(new AddedClass(file.getName(), bytes, classLoader));
                        }
                    } else if(change.getType() == WatchServiceFileSystemWatcher.FileChangeEvent.Type.MODIFIED) {
                        String hash = hashes.get(change.getFile().getCanonicalPath());
                        if(hash == null) {
                            //class is not loaded yet
                            continue;
                        }
                        try (FileInputStream in = new FileInputStream(change.getFile())) {
                            byte[] bytes = FileReader.readFileBytes(in);
                            if(!hash.equals(MD5.md5(bytes))) {
                                ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(bytes)));
                                changedClasses.add(new ClassDefinition(classLoader.loadClass(file.getName()), bytes));
                            }
                        }
                    }
                }
                Agent.redefine(changedClasses.toArray(new ClassDefinition[changedClasses.size()]), addedClasses.toArray(new AddedClass[addedClasses.size()]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    synchronized void addClassFile(String className, ClassLoader classLoader) {
        if(classLoader == null) {
            return;
        }
        URL resource = classLoader.getResource(className.replace(".", "/") + ".class");
        if(resource == null) {
            return;
        }
        File file = new File(resource.getFile());
        if(!file.exists()) {
            return;
        }

        int parentCount = 1;
        for(int i = 0; i < className.length(); ++i) {
            if(className.charAt(i) == '.' || className.charAt(i) == '/') {
                parentCount++;
            }
        }
        try (InputStream in = resource.openStream()) {
            hashes.put(file.getCanonicalPath(), MD5.md5(FileReader.readFileBytes(in)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for(int i = 0; i < parentCount; ++i) {
            file = file.getParentFile();
        }
        if(registered.contains(file)) {
            return;
        }
        registered.add(file);

        ClassLoaderData classLoaderData = ClassLoaderData.get(classLoader);
        Callback callback = classLoaderData.getAttachment(callbackAttachmentKey);
        if(callback == null) {
            classLoaderData.putAttachment(callbackAttachmentKey, callback = new Callback(classLoader));
        }
        watcher.watchPath(file, callback);
    }


}
