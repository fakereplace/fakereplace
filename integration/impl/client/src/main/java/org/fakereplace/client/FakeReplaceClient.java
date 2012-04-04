package org.fakereplace.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

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
            for (int i = 0; i < deploymentName.length(); ++i) {
                output.writeChar(deploymentName.charAt(i));
            }
            output.writeInt(classes.size());
            for (Map.Entry<String, ClassData> entry : classes.entrySet()) {
                output.writeInt(entry.getKey().length());
                for (int i = 0; i < entry.getKey().length(); ++i) {
                    output.writeChar(entry.getKey().charAt(i));
                }
                output.writeLong(entry.getValue().getTimestamp());
            }
            output.writeInt(resources.size());
            for (Map.Entry<String, ResourceData> entry : resources.entrySet()) {
                final ResourceData data = entry.getValue();
                output.writeInt(data.getRelativePath().length());
                for (int i = 0; i < data.getRelativePath().length(); ++i) {
                    output.writeChar(data.getRelativePath().charAt(i));
                }
                output.writeLong(data.getTimestamp());
            }
            output.flush();
            final Set<String> classNames = new HashSet<String>();
            final Set<String> resourceNames = new HashSet<String>();
            readReplacable(input, classNames);
            readReplacable(input, resourceNames);

            output.flush();
            output.writeInt(classNames.size());
            for (String name : classNames) {
                final ClassData data = classes.get(name);
                output.writeInt(name.length());
                for (int i = 0; i < name.length(); ++i) {
                    output.writeChar(name.charAt(i));
                }
                byte[] bytes = data.getContentSource().getData();
                output.writeInt(bytes.length);
                output.write(bytes);
            }

            output.writeInt(resourceNames.size());
            for (final String resource : resourceNames) {
                final ResourceData data = resources.get(resource);
                output.writeInt(resource.length());
                for (int i = 0; i < resource.length(); ++i) {
                    output.writeChar(resource.charAt(i));
                }
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
            int length = input.readInt();
            char[] nameBuffer = new char[length];
            for (int pos = 0; pos < length; ++pos) {
                nameBuffer[pos] = input.readChar();
            }
            final String className = new String(nameBuffer);
            resourceNames.add(className);
        }
    }


    private static byte[] getBytesFromZip(ZipInputStream zip) throws IOException {
        // Get the size of the file
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();


        // Read in the bytes
        int numRead = 0;
        byte[] bytes = new byte[1024];
        while ((numRead = zip.read(bytes)) >= 0) {
            stream.write(bytes, 0, numRead);
        }

        return stream.toByteArray();
    }

}
