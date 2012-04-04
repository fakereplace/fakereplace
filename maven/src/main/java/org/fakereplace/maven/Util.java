package org.fakereplace.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stuart Douglas
 */
public class Util {


    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            return getBytesFromStream(is);
        } finally {
            is.close();
        }
    }

    public static byte[] getBytesFromStream(InputStream is) throws IOException {
        // Create the byte array to hold the data
        final ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());

        int read = -1;
        final byte[] buff = new byte[512];
        while ((read = is.read(buff)) != -1) {
            out.write(buff, 0, read);
        }
        return out.toByteArray();
    }
}
