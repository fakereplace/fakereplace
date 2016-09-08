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

package org.fakereplace.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple client side implementation of the fakereplace protocol
 *
 * @author Stuart Douglas
 */
public class FakeReplaceClient {

    public static void run(final String deploymentName, Map<String, ClassData> classes, final Map<String, ResourceData> resources) throws IOException {
        final Socket socket = new Socket("localhost", 6555);
        try {
            run(socket, deploymentName, classes, resources);
        } finally {
            socket.close();
        }
    }

    public static void run(Socket socket, final String deploymentName, Map<String, ClassData> classes, final Map<String, ResourceData> resources) {
        try {
            final DataInputStream input = new DataInputStream(socket.getInputStream());
            final DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(0xCAFEDEAF);
            output.writeInt(deploymentName.length());
            output.write(deploymentName.getBytes());
            output.writeInt(classes.size());
            for (Map.Entry<String, ClassData> entry : classes.entrySet()) {
                output.writeInt(entry.getKey().length());
                output.write(entry.getKey().getBytes());
                output.writeLong(entry.getValue().getTimestamp());
            }
            output.writeInt(resources.size());
            for (Map.Entry<String, ResourceData> entry : resources.entrySet()) {
                final ResourceData data = entry.getValue();
                output.writeInt(data.getRelativePath().length());
                output.write(data.getRelativePath().getBytes());
                output.writeLong(data.getTimestamp());
            }
            output.flush();
            final Set<String> classNames = new HashSet<String>();
            final Set<String> resourceNames = new HashSet<String>();
            readReplacable(input, classNames);
            readReplacable(input, resourceNames);

            if(classNames.isEmpty()) {
                System.out.println("No updated classes found to replace");
            } else {
                System.out.println("Updating " + classNames.size() + " classes");
            }

            output.flush();
            output.writeInt(classNames.size());
            for (String name : classNames) {
                final ClassData data = classes.get(name);
                output.writeInt(name.length());
                output.write(name.getBytes());
                byte[] bytes = data.getContentSource().getData();
                output.writeInt(bytes.length);
                output.write(bytes);
            }

            output.writeInt(resourceNames.size());
            for (final String resource : resourceNames) {
                final ResourceData data = resources.get(resource);
                output.writeInt(resource.length());
                output.write(resource.getBytes());
                byte[] bytes = data.getContentSource().getData();
                output.writeInt(bytes.length);
                output.write(bytes);
            }

            output.flush();

            int result = input.readInt();
            if(result != 0) {
                System.out.println("Replacement failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void readReplacable(final DataInputStream input, final Set<String> resourceNames) throws IOException {
        int noResources = input.readInt();
        for (int i = 0; i < noResources; ++i) {
            final String className = readString(input);
            resourceNames.add(className);
        }
    }

    private static String readString(final DataInputStream input) throws IOException {
        int toread = input.readInt();
        byte [] buf = new byte[toread];
        int read = 0;
        while (toread > 0 && (read = input.read(buf, read, toread)) != -1) {
            toread -= read;
        }
        return new String(buf);
    }
}
