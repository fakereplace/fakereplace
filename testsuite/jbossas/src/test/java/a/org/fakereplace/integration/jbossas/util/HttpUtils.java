package a.org.fakereplace.integration.jbossas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;

/**
 * @author Stuart Douglas
 */
public class HttpUtils {

    public static String getContent(HttpResponse response) throws IOException {
        InputStream stream = null;
        try {
            stream = response.getEntity().getContent();
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            char[] buff = new char[512];
            int read = -1;
            while ((read = reader.read(buff)) != -1) {
                builder.append(buff, 0, read);
            }
            return builder.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

}
