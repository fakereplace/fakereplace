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


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Stuart Douglas
 */
public class FakereplaceServer implements Runnable {

    private static final int DEFAULT_PORT = 6555;

    private final int port;

    /**
     * Server will listen on given port.
     */
    private FakereplaceServer(int port) {
        this.port = port;
    }

    /**
     * Server will listen on {@link FakereplaceServer#DEFAULT_PORT} port.
     */
    private FakereplaceServer() {
        this(DEFAULT_PORT);
    }

    @Override
    public void run() {
        try {
            final ServerSocket socket = new ServerSocket(port);
            System.out.println("Fakereplace listening on port " + port);
            while (true) {
                try {
                    final Socket realSocket = socket.accept();
                    FakereplaceProtocol.run(realSocket);
                } catch (Throwable t) {
                    System.err.println("Fakereplace server error");
                    t.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println("Fakereplace server could not start");
            e.printStackTrace();
        }
    }

    /**
     * @param portParam must be parseable integer or null
     * @throws NumberFormatException when {@code port} is not a valid integer
     */
    static FakereplaceServer createServer(String portParam) {
        final FakereplaceServer server;
        if (portParam == null) {
            server = new FakereplaceServer();
        } else {
            int port = Integer.parseInt(portParam);
            if (port < 1024 || port >= 65536) {
                throw new IllegalArgumentException("Port is out of range. Must be 1024 <= port < 65536.");
            }
            server = new FakereplaceServer(port);
        }
        return server;
    }

    /**
     * Creates and starts {@link FakereplaceServer} as a daemon thread listening on given port.
     *
     * @param portParam when equal to {@code -1}, no server nor Thread is created
     *                  when null, server will listen on {@link FakereplaceServer#DEFAULT_PORT} port
     *                  when
     */
    public static void startFakereplaceServerDaemonThread(String portParam) {
        if (portParam == null || !portParam.equals("-1")) {
            Thread thread = new Thread(FakereplaceServer.createServer(portParam));
            thread.setDaemon(true);
            thread.setName("Fakereplace Thread");
            thread.start();
        } else {
            System.out.println("Fakereplace is running.");
        }
    }
}
