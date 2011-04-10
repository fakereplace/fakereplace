package org.fakereplace.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FileReader {
    public static byte[] readFileBytes(InputStream in) {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream(in.available());
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                st.write(buf, 0, len);
            }
            return st.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
