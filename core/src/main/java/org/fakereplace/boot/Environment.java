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
import java.net.URL;

/**
 * class that stores some basic enviroment info.
 *
 * @author stuart
 */
public class Environment {

    protected static volatile Environment environment = new Environment();

    protected static final String dumpDirectory;

    protected static final String[] replaceablePackages;

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

    public boolean isClassReplacable(String className, ClassLoader loader) {
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

    public String getDumpDirectory() {
        return dumpDirectory;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    public static void setEnvironment(final Environment environment) {
        Environment.environment = environment;
    }
}
