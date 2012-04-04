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
