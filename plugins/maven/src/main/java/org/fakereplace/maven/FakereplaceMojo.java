/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
