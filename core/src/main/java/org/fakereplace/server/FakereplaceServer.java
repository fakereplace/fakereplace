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
