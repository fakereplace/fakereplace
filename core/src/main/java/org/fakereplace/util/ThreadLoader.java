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

package org.fakereplace.util;

/**
 * This class can be used to load another class a create an instance. If the java
 * agent loads a class directly, it is not instrumented.
 *
 * @author stuart
 */
public class ThreadLoader implements Runnable {
    private final String className;
    private final ClassLoader classLoader;
    private final boolean createClass;

    private ThreadLoader(String className, ClassLoader classLoader, boolean createClass) {
        this.className = className;
        this.classLoader = classLoader;
        this.createClass = createClass;
    }

    public void run() {
        try {
            Class<?> c = classLoader.loadClass(className);
            if (createClass) {
                c.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadAsync(String className, ClassLoader classLoader, boolean create) {
        new Thread(new ThreadLoader(className, classLoader, create)).start();
    }

}
