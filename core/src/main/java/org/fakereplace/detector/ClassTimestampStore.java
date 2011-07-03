package org.fakereplace.detector;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that stores information about the timestamps of replacable classes
 *
 * @author Stuart Douglas
 */
public class ClassTimestampStore {

    private static final Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();

    public static void recordTimestamp(String className, ClassLoader loader) {
        if(loader == null) {
            return;
        }
        final URL file = loader.getResource(className.replace(".", "/") + ".class");
        if (file != null) {
            URLConnection connection = null;
            try {
                connection = file.openConnection();
                timestamps.put(className, connection.getLastModified());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Set<String> getUpdatedClasses(Map<String, Long> updatedClasses) {
        final Set<String> ret = new HashSet<String>();
        for(Map.Entry<String, Long> entry : updatedClasses.entrySet()) {
            if(!timestamps.containsKey(entry.getKey())) {
                ret.add(entry.getKey());
            } else if(timestamps.get(entry.getKey()) < entry.getValue()) {
                ret.add(entry.getKey());
            }
        }
        return ret;
    }
}
