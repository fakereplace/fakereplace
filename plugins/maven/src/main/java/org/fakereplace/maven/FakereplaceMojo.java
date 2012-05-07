/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
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

package org.fakereplace.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.fakereplace.client.ClassData;
import org.fakereplace.client.ContentSource;
import org.fakereplace.client.FakeReplaceClient;
import org.fakereplace.client.ResourceData;

/**
 * @author Stuart Douglas
 * @goal fakereplace
 */
public class FakereplaceMojo extends AbstractMojo {


    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private String path;


    /**
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(project.getArtifact() == null || project.getArtifact().getFile() == null) {
            throw new IllegalStateException("You must run mvn package before the fakereplace plugin runs, e.g. mvn package fakereplace:fakereplace");
        }
        final String fileName = project.getArtifact().getFile().getName();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(project.getArtifact().getFile());
            final File file = new File(path);
            final Map<String, ClassData> classes = new HashMap<String, ClassData>();
            final Map<String, ResourceData> resources = new HashMap<String, ResourceData>();
            handleClassesDirectory(file, file, classes);


            handleArtifact(zipFile, resources);

            FakeReplaceClient.run(fileName, classes, resources);

        } catch (Throwable t) {
            getLog().error("Error running fakereplace: ", t);
        } finally {
            if(zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleArtifact(final ZipFile zipFile, final Map<String, ResourceData> resources) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                resources.put(entry.getName(), new ResourceData(entry.getName(), entry.getTime(), new ContentSource() {
                    @Override
                    public byte[] getData() throws IOException {
                        final InputStream stream = zipFile.getInputStream(entry);
                        try {
                            return Util.getBytesFromStream(stream);
                        } finally {
                            stream.close();
                        }
                    }
                }));
            }
        }
    }

    private void handleClassesDirectory(File base, File dir, Map<String, ClassData> classes) {
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                handleClassesDirectory(base, file, classes);
            } else if (file.getName().endsWith(".class")) {
                final String relFile = file.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
                final String className = relFile.substring(0, relFile.length() - ".class".length()).replace("/", ".");
                classes.put(className, new ClassData(className, file.lastModified(), new ContentSource() {
                    @Override
                    public byte[] getData() throws IOException {
                        return Util.getBytesFromFile(file);
                    }
                }));
            }
        }
    }


}
