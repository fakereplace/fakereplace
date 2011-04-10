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

package org.fakereplace;

import org.fakereplace.api.IntegrationInfo;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * class that is responsible for loading any bundled integrations from
 * the fakereplace archive
 * <p/>
 * We cannot use a normal service loader approach here, as everything can be
 * bundled
 * into a super jar. Instead we look for classes that take the form:
 * <p/>
 * org.fakereplace.integration.$1.$1IntegrationInfo
 * <p/>
 * and load them
 *
 * @author stuart
 */
public class IntegrationLoader {

    public static final String INTEGRATION_PACKAGE = "org/fakereplace/integration";

    public static Set<IntegrationInfo> getIntegrationInfo(ClassLoader clr) {
        Set<IntegrationInfo> integrations = new HashSet<IntegrationInfo>();
        Set<String> intPackages = new HashSet<String>();
        try {
            Enumeration<URL> urlJars = clr.getResources(INTEGRATION_PACKAGE);
            while (urlJars.hasMoreElements()) {
                URL urlJar = urlJars.nextElement();

                if (urlJar != null) {
                    try {
                        URLConnection urlConnection = urlJar.openConnection();
                        // in tests sometimes it can pick up integrations from the CP
                        // rather than the jar
                        if (urlConnection instanceof JarURLConnection) {
                            JarURLConnection conn = (JarURLConnection) urlJar.openConnection();
                            JarFile file = conn.getJarFile();
                            Enumeration<JarEntry> it = file.entries();
                            while (it.hasMoreElements()) {
                                JarEntry entry = it.nextElement();
                                if (entry.getName().contains(INTEGRATION_PACKAGE)) {
                                    String end = entry.getName().substring(INTEGRATION_PACKAGE.length() + 1);
                                    if (end.length() > 0) {
                                        String subPack = end.substring(0, end.indexOf('/'));
                                        intPackages.add(subPack);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (String s : intPackages) {
                IntegrationInfo info = loadIntegration(s);
                if (info != null) {
                    integrations.add(info);
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return integrations;
    }

    private static IntegrationInfo loadIntegration(String name) {
        String integrationClass = "org.fakereplace.integration." + name + "." + name.substring(0, 1).toUpperCase() + name.substring(1) + "IntegrationInfo";
        try {
            Class<?> cls = Class.forName(integrationClass);
            IntegrationInfo info = (IntegrationInfo) cls.newInstance();
            return info;
        } catch (ClassNotFoundException e) {
            System.out.println("COULD NOT LOAD INTEGRATION CLASS: " + integrationClass);
        } catch (InstantiationException e) {
            System.out.println("COULD NOT INSTANCIATE INTEGRATION CLASS: " + integrationClass);
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException CREATING INTEGRATION CLASS: " + integrationClass);
        } catch (ClassCastException e) {
            System.out.println(integrationClass + " WAS NOT AN INSTANCE OF  IntegrationInfo");
        }
        return null;
    }

}
