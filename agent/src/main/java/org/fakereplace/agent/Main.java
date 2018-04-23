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

package org.fakereplace.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

public class Main {

    private static final String WEB_RESOURCES_DIR = "web.resources.dir";
    private static final String SRCS_DIR = "srcs.dir";
    private static final String CLASSES_DIR = "classes.dir";
    private static final String REMOTE_PASSWORD = "remote.password";
    private static final int CLASS_CHANGE_RESPONSE = 3;

    public static void main(String... args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar agent.jar /path/to/class-change.properties http(s)://remotehost:port/path");
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        String pw = props.getProperty(REMOTE_PASSWORD);
        if (pw == null) {
            System.out.println("No remote password in supplied properties file");
            System.exit(1);
        }
        String web = props.getProperty(WEB_RESOURCES_DIR);
        String srcs = props.getProperty(SRCS_DIR);
        String classes = props.getProperty(CLASSES_DIR);


        if (web == null && srcs == null && classes == null) {
            System.out.println("No local locations specified to check for changes, exiting");
            System.exit(1);
        }
        try {
            ContainerProvider.getWebSocketContainer().connectToServer(new AgentEndpoint(web, srcs, classes), ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    headers.put(REMOTE_PASSWORD, Collections.singletonList(pw));
                }
            }).build(), new URI(args[1]));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        for (; ; ) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {

            }
        }
    }

    private static final class AgentEndpoint extends Endpoint implements MessageHandler.Whole<byte[]> {
        private final String web;
        private final String srcs;
        private final String classes;
        private volatile Session session;

        private final Map<String, Long> classChangeTimes = new ConcurrentHashMap<>();
        private final Map<String, Long> resourceChangeTimes = new ConcurrentHashMap<>();

        private AgentEndpoint(String web, String srcs, String classes) {
            this.web = web;
            this.srcs = srcs;
            this.classes = classes;
        }


        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {
            session.addMessageHandler(this);
            this.session = session;
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("Connection closed " + closeReason);
            System.exit(0);
        }

        @Override
        public void onError(Session session, Throwable thr) {
            thr.printStackTrace();
            System.exit(1);
        }

        @Override
        public void onMessage(byte[] bytes) {
            switch (bytes[0]) {
                case 1: {
                    handleInitialData(bytes);
                    break;
                }
                case 2: {
                    sendChangedClasses();
                    break;
                }
                default: {
                    System.out.println("Ignoring unknown message type " + bytes[0]);
                }
            }
        }

        private void sendChangedClasses() {
            final Map<String, byte[]> changedClasses = new HashMap<>();
            final Map<String, byte[]> changedSrcs = new HashMap<>();
            final Map<String, byte[]> changedResources = new HashMap<>();
            try (OutputStream out = session.getBasicRemote().getSendStream()) {
                if (srcs != null) {
                    scanForClassChanges("", new File(srcs), changedSrcs);
                } else if (classes != null) {
                    scanForClassChanges("", new File(classes), changedSrcs);
                }
                if (web != null) {
                    scanForWebResources("", new File(web), changedResources);
                }

                out.write(CLASS_CHANGE_RESPONSE);
                DataOutputStream data = new DataOutputStream(new DeflaterOutputStream(out));
                data.writeInt(changedSrcs.size());
                for (Map.Entry<String, byte[]> entry : changedSrcs.entrySet()) {
                    data.writeUTF(entry.getKey());
                    data.writeInt(entry.getValue().length);
                    data.write(entry.getValue());
                }
                data.writeInt(changedClasses.size());
                for (Map.Entry<String, byte[]> entry : changedClasses.entrySet()) {
                    data.writeUTF(entry.getKey());
                    data.writeInt(entry.getValue().length);
                    data.write(entry.getValue());
                }
                data.writeInt(changedResources.size());
                for (Map.Entry<String, byte[]> entry : changedResources.entrySet()) {
                    data.writeUTF(entry.getKey());
                    data.writeInt(entry.getValue().length);
                    data.write(entry.getValue());
                }
                data.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        private void scanForWebResources(String currentPath, File root, Map<String, byte[]> found) throws IOException {
            if (currentPath.equals("WEB-INF")) {
                return;
            }
            File serverCurrent = new File(root, currentPath);
            for (String part : serverCurrent.list()) {
                String fullPart = (currentPath.isEmpty()? "" : (currentPath + File.separatorChar)) + part;
                File f = new File(serverCurrent, part);
                if (f.isDirectory()) {
                    scanForWebResources(fullPart, root, found);
                } else {
                    File localFile = new File(serverCurrent, part);
                    Long serverChange = resourceChangeTimes.get(fullPart);
                    long lastModified = localFile.lastModified();
                    if (serverChange == null || serverChange < lastModified) {
                        found.put(fullPart, readFile(localFile));
                        resourceChangeTimes.put(fullPart, lastModified);
                    }
                }
            }
        }
        private void scanForClassChanges(String currentPath, File root, Map<String, byte[]> found) throws IOException {
            File serverCurrent = new File(root, currentPath);
            for (String part : serverCurrent.list()) {
                String fullPart = (currentPath.isEmpty()? "" : (currentPath + File.separatorChar)) + part;
                File f = new File(serverCurrent, part);
                if (f.isDirectory()) {
                    scanForClassChanges(fullPart, root, found);
                } else if(part.contains(".")){
                    File localFile = new File(serverCurrent, part);
                    String fullPartAsClassFile = fullPart.substring(0, fullPart.lastIndexOf('.')) + ".class";

                    Long serverChange = classChangeTimes.get(fullPartAsClassFile);
                    long lastModified = localFile.lastModified();
                    if (serverChange == null || serverChange < lastModified) {
                        found.put(fullPart, readFile(localFile));
                        classChangeTimes.put(fullPartAsClassFile, lastModified);
                    }
                }
            }
        }

        private byte[] readFile(File localFile) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (FileInputStream in = new FileInputStream(localFile)) {
                byte[] buf = new byte[1024];
                int r;
                while ((r = in.read(buf)) > 0) {
                    out.write(buf, 0, r);
                }
            }
            return out.toByteArray();
        }

        private void handleInitialData(byte[] bytes) {
            try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes, 1, bytes.length - 1)))) {
                int count = in.readInt();
                for (int i = 0; i < count; ++i) {
                    String key = in.readUTF();
                    long value = in.readLong();
                    classChangeTimes.put(key, value);
                }
                count = in.readInt();
                for (int i = 0; i < count; ++i) {
                    String key = in.readUTF();
                    long value = in.readLong();
                    resourceChangeTimes.put(key, value);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }


}
