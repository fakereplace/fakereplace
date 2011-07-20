/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.boot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class that stores some basic environment info.
 *
 * @author stuart
 */
public class DefaultEnvironment implements Environment {

    protected static final String dumpDirectory;

    protected static final String[] replaceablePackages;

    protected static volatile Environment environment = new DefaultEnvironment();

    private static final Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();
    private static final Map<String, ClassLoader> loaders = new ConcurrentHashMap<String, ClassLoader>();

    static {
        String dump = System.getProperty(Constants.DUMP_DIRECTORY_KEY);
        if (dump != null) {
            File f = new File(dump);
            if (!f.exists()) {
                System.out.println("dump directory  " + dump + " does not exist ");
                dumpDirectory = null;
            } else {
                dumpDirectory = dump;
                System.out.println("dumping class definitions to " + dump);
            }
        } else {
            dumpDirectory = null;
        }
        String plist = System.getProperty(Constants.REPLACEABLE_PACKAGES_KEY);
        if (plist == null || plist.length() == 0) {
            replaceablePackages = new String[0];
        } else {
            replaceablePackages = plist.split(",");
        }
    }

    @Override
    public boolean isClassReplaceable(String className, ClassLoader loader) {
        for (String i : replaceablePackages) {
            if (className.startsWith(i)) {
                return true;
            }
        }
        if (className.contains("$Proxy")) {
            return true;
        }
        if (loader != null) {
            URL u = loader.getResource(className.replace('.', '/') + ".class");
            if (u != null) {
                if (u.getProtocol().equals("file") || u.getProtocol().equals("vfsfile")) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public String getDumpDirectory() {
        return dumpDirectory;
    }

    /**
     * todo: move this somewhere else
     *
     * @return
     */
    public static Environment getEnvironment() {
        return environment;
    }

    public static void setEnvironment(final Environment environment) {
        DefaultEnvironment.environment = environment;
    }

    public void recordTimestamp(String className, ClassLoader loader) {
        if (loader == null) {
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


    public Set<Class> getUpdatedClasses(final String deploymentName, Map<String, Long> updatedClasses) {
        final Set<Class> ret = new HashSet<Class>();
        for (Map.Entry<String, Long> entry : updatedClasses.entrySet()) {
            if (timestamps.containsKey(entry.getKey()) && timestamps.get(entry.getKey()) < entry.getValue()) {
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
