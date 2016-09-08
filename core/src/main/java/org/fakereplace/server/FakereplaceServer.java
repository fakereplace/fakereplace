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

    private final int port;

    public FakereplaceServer(int port) {
        this.port = port;
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
}
