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
    private static final Map<String, ClassLoader> loaders = new ConcurrentHashMap<String, ClassLoader>();

    public static void recordTimestamp(String className, ClassLoader loader) {
        if(loader == null) {
            return;
        }
        final URL file = loader.getResource(className.replace(".", "/") + ".class");
        className = className.replace("/", ".");
        if (file != null) {
            URLConnection connection = null;
            try {
                connection = file.openConnection();
                timestamps.put(className, connection.getLastModified());
                loaders.put(className, loader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Set<Class> getUpdatedClasses(Map<String, Long> updatedClasses) {
        final Set<Class> ret = new HashSet<Class>();
        for(Map.Entry<String, Long> entry : updatedClasses.entrySet()) {
            if(timestamps.containsKey(entry.getKey()) && timestamps.get(entry.getKey()) < entry.getValue()) {
                ClassLoader loader = loaders.get(entry.getKey());
                try {
                    ret.add(loader.loadClass(entry.getKey()));
                    timestamps.put(entry.getKey(), entry.getValue());
                } catch (ClassNotFoundException e) {
                    System.err.println("Could not load class " + entry);
                }
            }
        }
        return ret;
    }
}
