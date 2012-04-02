/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.fakereplace.integration.jbossas;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.boot.Environment;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Services;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.vfs.VirtualFile;

/**
 * @author Stuart Douglas
 */
public class JBossAsEnvironment implements Environment {

    @Override
    public boolean isClassReplaceable(final String className, final ClassLoader loader) {
        if (loader instanceof ModuleClassLoader) {
            if (((ModuleClassLoader) loader).getModule().getIdentifier().toString().startsWith("deployment.")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDumpDirectory() {
        return null;
    }

    private final Map<ModuleIdentifier, ModuleClassLoader> loadersByModuleIdentifier = new ConcurrentHashMap<ModuleIdentifier, ModuleClassLoader>();


    private final Map<ModuleClassLoader, Map<String, Long>> timestamps = new ConcurrentHashMap<ModuleClassLoader, Map<String, Long>>();

    public void recordTimestamp(String className, ClassLoader loader) {
        if (!(loader instanceof ModuleClassLoader)) {
            return;
        }
        Map<String, Long> stamps = null;
        final ModuleClassLoader moduleClassLoader = (ModuleClassLoader) loader;
        final ModuleIdentifier moduleIdentifier = moduleClassLoader.getModule().getIdentifier();
        if (loadersByModuleIdentifier.containsKey(moduleIdentifier)) {
            final ModuleClassLoader oldLoader = loadersByModuleIdentifier.get(moduleIdentifier);
            if (oldLoader != moduleClassLoader) {
                loadersByModuleIdentifier.put(moduleIdentifier, moduleClassLoader);
                timestamps.put(moduleClassLoader, stamps = new ConcurrentHashMap<String, Long>());
            } else {
                stamps = timestamps.get(moduleClassLoader);
            }
        } else {
            loadersByModuleIdentifier.put(moduleIdentifier, moduleClassLoader);
            timestamps.put(moduleClassLoader, stamps = new ConcurrentHashMap<String, Long>());
        }

        final URL file = loader.getResource(className.replace(".", "/") + ".class");
        className = className.replace("/", ".");
        if (file != null) {
            URLConnection connection = null;
            try {
                connection = file.openConnection();
                stamps.put(className, connection.getLastModified());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public Set<Class> getUpdatedClasses(final String deploymentName, Map<String, Long> updatedClasses) {

        final ModuleIdentifier moduleId = getModuleIdentifier(deploymentName);
        final ModuleClassLoader loader = loadersByModuleIdentifier.get(moduleId);
        if (loader == null) {
            return Collections.emptySet();
        }
        final Map<String, Long> timestamps = this.timestamps.get(loader);

        final Set<Class> ret = new HashSet<Class>();
        for (Map.Entry<String, Long> entry : updatedClasses.entrySet()) {
            if (timestamps.containsKey(entry.getKey()) && timestamps.get(entry.getKey()) < entry.getValue()) {
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

    @Override
    public Set<String> getUpdatedResources(final String deploymentName, final Map<String, Long> updatedResources) {
        final ModuleIdentifier moduleId = getModuleIdentifier(deploymentName);
        final ModuleClassLoader loader = loadersByModuleIdentifier.get(moduleId);
        if (loader == null) {
            return Collections.emptySet();
        }


        final DeploymentUnit deploymentUnit = (DeploymentUnit) CurrentServiceContainer.getServiceContainer().getRequiredService(Services.deploymentUnitName(deploymentName)).getValue();
        final ResourceRoot root = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);

        final Set<String> resources = new HashSet<String>();
        for (final Map.Entry<String, Long> entry : updatedResources.entrySet()) {
            final VirtualFile file = root.getRoot().getChild(entry.getKey());
            if (file.exists()) {
                long last = file.getLastModified();
                if (entry.getValue() > last) {
                    resources.add(entry.getKey());
                }
            }
        }
        return resources;
    }

    @Override
    public void updateResource(final String archiveName, final Map<String, byte[]> replacedResources) {
        final ModuleIdentifier moduleId = getModuleIdentifier(archiveName);
        final ModuleClassLoader loader = loadersByModuleIdentifier.get(moduleId);
        if (loader == null) {
            return;
        }

        final DeploymentUnit deploymentUnit = (DeploymentUnit) CurrentServiceContainer.getServiceContainer().getRequiredService(Services.deploymentUnitName(archiveName)).getValue();
        final ResourceRoot root = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);

        for (final Map.Entry<String, byte[]> entry : replacedResources.entrySet()) {
            final VirtualFile file = root.getRoot().getChild(entry.getKey());
            try {
                final FileOutputStream stream = new FileOutputStream(file.getPhysicalFile(), false);
                try {
                    stream.write(entry.getValue());
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterReplacement(final Set<Class<?>> classes, final String archiveName) {

    }


    private ModuleIdentifier getModuleIdentifier(final String deploymentArchive) {
        return ModuleIdentifier.create("deployment." + deploymentArchive);
    }

}
