/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
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
