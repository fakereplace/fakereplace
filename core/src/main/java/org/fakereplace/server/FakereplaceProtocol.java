package org.fakereplace.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fakereplace.Agent;
import org.fakereplace.boot.DefaultEnvironment;
import org.fakereplace.boot.Logger;
import org.fakereplace.replacement.AddedClass;

/**
 * An implementation of the fakereplace client server protocol.
 * <p/>
 * The basic protocol is as follows:
 * <p/>
 * Client -
 * Magic no 0xCAFEDEAF
 * no classes (int)
 * class data (1 per class)
 * class name length (int)
 * class name
 * timestamp (long)
 * <p/>
 * Server -
 * no classes (int)
 * class data (1 per class)
 * class name length
 * class name
 * <p/>
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

    public static void run(Socket socket) {
        DataOutputStream output = null;
        try {
            Logger.trace(FakereplaceProtocol.class, "Fakereplace update is running");
            final DataInputStream input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            final Map<String, Long> classes = new HashMap<String, Long>();
            final Map<String, Long> resources = new HashMap<String, Long>();
            int magic = input.readInt();
            if (magic != 0xCAFEDEAF) {
                System.err.println("Fakereplace server error, wrong magic number");
                return;
            }
            final String archiveName = readString(input);

            readAvailable(input, classes);
            readAvailable(input, resources);

            Logger.log(FakereplaceProtocol.class, "Fakereplace is checking for updates classes. Client sent " + classes.size() + "classes");


            final Set<Class> classesToReplace = DefaultEnvironment.getEnvironment().getUpdatedClasses(archiveName, classes);
            final Map<String, Class> classMap = new HashMap<String, Class>();
            output.writeInt(classesToReplace.size());
            for (Class clazz : classesToReplace) {
                final String cname = clazz.getName();
                output.writeInt(cname.length());
                output.write(cname.getBytes());
                classMap.put(cname, clazz);
            }
            final Set<String> resourcesToReplace = DefaultEnvironment.getEnvironment().getUpdatedResources(archiveName, resources);
            output.writeInt(resourcesToReplace.size());
            for (String cname : resourcesToReplace) {
                output.writeInt(cname.length());
                output.write(cname.getBytes());
            }

            output.flush();

            final Set<ClassDefinition> classDefinitions = new HashSet<ClassDefinition>();
            final Set<Class<?>> replacedClasses = new HashSet<Class<?>>();
            int noClasses = input.readInt();
            for (int i = 0; i < noClasses; ++i) {
                final String className = readString(input);
                int length = input.readInt();
                byte[] buffer = new byte[length];
                for (int j = 0; j < length; ++j) {
                    buffer[j] = (byte) input.read();
                }
                classDefinitions.add(new ClassDefinition(classMap.get(className), buffer));
                replacedClasses.add(classMap.get(className));
            }

            final Map<String, byte[]> replacedResources = new HashMap<String, byte[]>();

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

            Agent.redefine(classDefinitions.toArray(new ClassDefinition[classDefinitions.size()]), new AddedClass[0]);
            DefaultEnvironment.getEnvironment().updateResource(archiveName, replacedResources);
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
        byte [] buf = new byte[toread];
        int read = 0;
        while (toread > 0 && (read = input.read(buf, read, toread)) != -1) {
            toread -= read;
        }
        return new String(buf);
    }

}
