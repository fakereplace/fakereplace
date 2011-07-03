package org.fakereplace.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Stuart Douglas
 * @goal fakereplace
 */
public class FakreplaceMojo extends AbstractMojo {

    private final class Data {
        private final String className;
        private final long timestamp;
        private final File file;

        public Data(String className, long timestamp, File file) {
            this.className = className;
            this.timestamp = timestamp;
            this.file = file;
        }
    }

    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private String path;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final File file = new File(path);
            final Map<String, Data> classes = new HashMap<String, Data>();
            handleDirectory(file, file, classes);

            final Socket socket = new Socket("localhost", 6555);

            run(socket, classes);

        } catch (Throwable t) {
            getLog().error("Error running fakereplace: ", t);
        }
    }

    private void handleDirectory(File base, File dir, Map<String, Data> classes) {
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                handleDirectory(base, file, classes);
            } else if (file.getName().endsWith(".class")) {
                final String relFile = file.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
                final String className = relFile.substring(0, relFile.length() - ".class".length()).replace("/", ".");
                classes.put(className, new Data(className, file.lastModified(), file));
            }
        }
    }


    public static void run(Socket socket, Map<String, Data> classes) {
        try {
            final DataInputStream input = new DataInputStream(socket.getInputStream());
            final DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(0xCAFEDEAF);
            output.writeInt(classes.size());
            for(Map.Entry<String, Data> entry : classes.entrySet()) {
                output.writeInt(entry.getKey().length());
                for(int i = 0; i < entry.getKey().length(); ++i) {
                    output.writeChar(entry.getKey().charAt(i));
                }
                output.writeLong(entry.getValue().timestamp);
            }
            output.flush();
            final Set<String> classNames = new HashSet<String>();

            int noClasses = input.readInt();
            for (int i = 0; i < noClasses; ++i) {
                int length = input.readInt();
                char[] nameBuffer = new char[length];
                for (int pos = 0; pos < length; ++pos) {
                    nameBuffer[pos] = input.readChar();
                }
                final String className = new String(nameBuffer);
                classNames.add(className);
            }



            output.flush();
            output.writeInt(classNames.size());
            for(String name : classNames) {
                final Data data = classes.get(name);
                output.writeInt(name.length());
                for(int i = 0; i < name.length(); ++i) {
                    output.writeChar(name.charAt(i));
                }
                byte[] bytes = getBytesFromFile(data.file);
                output.writeInt(bytes.length);
                output.write(bytes);

            }
            output.flush();

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

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


}
