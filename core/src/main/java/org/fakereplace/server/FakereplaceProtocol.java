package org.fakereplace.server;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import org.fakereplace.Agent;
import org.fakereplace.detector.ClassTimestampStore;
import org.fakereplace.replacement.AddedClass;

import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        try {
            final DataInputStream input = new DataInputStream(socket.getInputStream());
            final DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            final Map<String, Long> classes = new HashMap<String, Long>();
            int magic = input.readInt();
            if (magic != 0xCAFEDEAF) {
                System.err.println("Fakereplace server error, wrong magic number");
                return;
            }
            int noClasses = input.readInt();
            for (int i = 0; i < noClasses; ++i) {
                int length = input.readInt();
                char[] nameBuffer = new char[length];
                for (int pos = 0; pos < length; ++pos) {
                    nameBuffer[pos] = input.readChar();
                }
                final String className = new String(nameBuffer);
                long ts = input.readLong();
                classes.put(className, ts);
            }
            final Set<String> classesToReplace = ClassTimestampStore.getUpdatedClasses(classes);

            output.writeInt(classesToReplace.size());
            for (String cname : classesToReplace) {
                output.writeInt(cname.length());
                for (int i = 0; i < cname.length(); ++i) {
                    output.writeChar(cname.charAt(i));
                }
            }
            output.flush();

            final Set<ClassDefinition> classDefinitions = new HashSet<ClassDefinition>();
            noClasses = input.readInt();
            for (int i = 0; i < noClasses; ++i) {
                int length = input.readInt();
                char[] nameBuffer = new char[length];
                for (int pos = 0; pos < length; ++pos) {
                    nameBuffer[pos] = input.readChar();
                }
                final String className = new String(nameBuffer);
                length = input.readInt();
                byte[] buffer = new byte[length];
                for (int j = 0; j < length; ++j) {
                    buffer[j] = (byte) input.read();
                }
            }

            Agent.redefine(classDefinitions.toArray(new ClassDefinition[classDefinitions.size()]), new AddedClass[0]);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
