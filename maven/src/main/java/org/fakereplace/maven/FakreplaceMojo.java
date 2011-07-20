package org.fakereplace.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stuart Douglas
 * @goal fakereplace
 */
public class FakreplaceMojo extends AbstractMojo {

    private final class ClassData {
        private final String className;
        private final long timestamp;
        private final File file;

        public ClassData(String className, File file) {
            this.className = className;
            this.timestamp = file.lastModified();
            this.file = file;
        }
    }

    private final class ResourceData {
        private final long timestamp;
        private final File file;
        private final String relativePath;

        public ResourceData(File file, final String relativePath) {
            this.relativePath = relativePath;
            this.timestamp = file.lastModified();
            this.file = file;
        }
    }


    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private String path;


    /**
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String fileName = project.getArtifact().getFile().getName();
        try {
            final File file = new File(path);
            final Map<String, ClassData> classes = new HashMap<String, ClassData>();
            final List<ResourceData> resources = new ArrayList<ResourceData>();
            handleClassesDirectory(file, file, classes);
            handleArtifact(project.getArtifact().getFile(), resources);
            final Socket socket = new Socket("localhost", 6555);

            run(socket, fileName, classes, resources);

        } catch (Throwable t) {
            getLog().error("Error running fakereplace: ", t);
        }
    }

    private void handleArtifact(final File file, final List<ResourceData> resources) {

    }

    private void handleClassesDirectory(File base, File dir, Map<String, ClassData> classes) {
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                handleClassesDirectory(base, file, classes);
            } else if (file.getName().endsWith(".class")) {
                final String relFile = file.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
                final String className = relFile.substring(0, relFile.length() - ".class".length()).replace("/", ".");
                classes.put(className, new ClassData(className, file));
            }
        }
    }


    public static void run(Socket socket, final String fileName, Map<String, ClassData> classes, final List<ResourceData> resources) {
        try {
            final DataInputStream input = new DataInputStream(socket.getInputStream());
            final DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(0xCAFEDEAF);
            output.writeInt(fileName.length());
            for (int i = 0; i < fileName.length(); ++i) {
                    output.writeChar(fileName.charAt(i));
                }
            output.writeInt(classes.size());
            for (Map.Entry<String, ClassData> entry : classes.entrySet()) {
                output.writeInt(entry.getKey().length());
                for (int i = 0; i < entry.getKey().length(); ++i) {
                    output.writeChar(entry.getKey().charAt(i));
                }
                output.writeLong(entry.getValue().timestamp);
            }
            output.writeInt(resources.size());
            for (ResourceData data : resources) {
                output.writeInt(data.relativePath.length());
                for (int i = 0; i < data.relativePath.length(); ++i) {
                    output.writeChar(data.relativePath.charAt(i));
                }
                output.writeLong(data.timestamp);
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
            for (String name : classNames) {
                final ClassData data = classes.get(name);
                output.writeInt(name.length());
                for (int i = 0; i < name.length(); ++i) {
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
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


}
