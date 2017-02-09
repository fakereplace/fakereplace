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

package org.fakereplace.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.api.environment.ChangedClasses;
import org.fakereplace.api.environment.CurrentEnvironment;
import org.fakereplace.core.Agent;
import org.fakereplace.logging.Logger;
import org.fakereplace.replacement.AddedClass;

/**
 * An implementation of the fakereplace client server protocol.
 * <p>
 * The basic protocol is as follows:
 * <p>
 * Client -
 * Magic no 0xCAFEDEAF
 * no classes (int)
 * class data (1 per class)
 * class name length (int)
 * class name
 * timestamp (long)
 * <p>
 * Server -
 * no classes (int)
 * class data (1 per class)
 * class name length
 * class name
 * <p>
 * Client -
 * no classes (int)
 * class data
 * class name length
 * class name
 * class bytes length
 * class bytes
 *
 * @author Stuart Douglas
 */
public class FakereplaceProtocol {

    private static final Logger log = Logger.getLogger(FakereplaceProtocol.class);

    public static void run(Socket socket) {
        DataOutputStream output = null;
        try {
            log.trace("Fakereplace update is running");
            final DataInputStream input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            final Map<String, Long> classes = new HashMap<>();
            final Map<String, Long> resources = new HashMap<>();
            int magic = input.readInt();
            if (magic != 0xCAFEDEAF) {
                System.err.println("Fakereplace server error, wrong magic number");
                return;
            }
            final String archiveName = readString(input);

            readAvailable(input, classes);
            readAvailable(input, resources);



            final ChangedClasses classesToReplace = CurrentEnvironment.getEnvironment().getUpdatedClasses(archiveName, classes);

            log.info("Fakereplace is checking for updates classes. Client sent " + classes.size() + " classes, " + classesToReplace.getChanged().size() + " need to be replaced");
            final Map<String, Class> classMap = new HashMap<String, Class>();
            output.writeInt(classesToReplace.getChanged().size() + classesToReplace.getNewClasses().size());
            for (Class clazz : classesToReplace.getChanged()) {
                final String cname = clazz.getName();
                output.writeInt(cname.length());
                output.write(cname.getBytes());
                classMap.put(cname, clazz);
            }
            for (String cname : classesToReplace.getNewClasses()) {
                output.writeInt(cname.length());
                output.write(cname.getBytes());
            }
            final Set<String> resourcesToReplace = CurrentEnvironment.getEnvironment().getUpdatedResources(archiveName, resources);
            output.writeInt(resourcesToReplace.size());
            for (String cname : resourcesToReplace) {
                output.writeInt(cname.length());
                output.write(cname.getBytes());
            }

            output.flush();

            final Set<ClassDefinition> classDefinitions = new HashSet<>();
            final Set<Class<?>> replacedClasses = new HashSet<>();
            final List<AddedClass> addedClassList = new ArrayList<>();
            int noClasses = input.readInt();
            for (int i = 0; i < noClasses; ++i) {
                final String className = readString(input);
                int length = input.readInt();
                byte[] buffer = new byte[length];
                for (int j = 0; j < length; ++j) {
                    buffer[j] = (byte) input.read();
                }
                final Class theClass = classMap.get(className);
                if (theClass != null) {
                    classDefinitions.add(new ClassDefinition(theClass, buffer));
                    replacedClasses.add(theClass);
                } else {
                    addedClassList.add(new AddedClass(className, buffer, classesToReplace.getClassLoader()));
                }
            }

            final Map<String, byte[]> replacedResources = new HashMap<>();

            int noResources = input.readInt();
            for (int i = 0; i < noResources; ++i) {
                final String resourceName = readString(input);
                int length = input.readInt();
                byte[] buffer = new byte[length];
                for (int j = 0; j < length; ++j) {
                    buffer[j] = (byte) input.read();
                }
                replacedResources.put(resourceName, buffer);
            }

            Agent.redefine(classDefinitions.toArray( new ClassDefinition[classDefinitions.size()]), addedClassList.toArray(new AddedClass[addedClassList.size()]));
            CurrentEnvironment.getEnvironment().updateResource(archiveName, replacedResources);
            output.writeInt(0);
        } catch (Exception e) {
            try {
                output.writeInt(1);
            } catch (IOException e1) {
                //ignore
            }
            e.printStackTrace();
        } finally {
            try {
                //write the result to
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readAvailable(final DataInputStream input, final Map<String, Long> resources) throws IOException {
        int noResources = input.readInt();
        for (int i = 0; i < noResources; ++i) {
            final String resourceName = readString(input);
            long ts = input.readLong();
            resources.put(resourceName, ts);
        }
    }

    private static String readString(final DataInputStream input) throws IOException {
        int toread = input.readInt();
        byte[] buf = new byte[toread];
        int read = 0;
        while (toread > 0 && (read = input.read(buf, read, toread)) != -1) {
            toread -= read;
        }
        return new String(buf);
    }

}
