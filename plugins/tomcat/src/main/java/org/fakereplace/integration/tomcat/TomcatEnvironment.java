package org.fakereplace.integration.tomcat;

import org.apache.catalina.core.StandardContext;
import org.fakereplace.api.environment.ChangedClasses;
import org.fakereplace.api.environment.Environment;
import org.fakereplace.core.AgentOption;
import org.fakereplace.core.AgentOptions;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.logging.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bogdan Mustiata &lt;bogdan.mustiata@gmail.com&gt;
 */
public class TomcatEnvironment implements Environment {
    private static final Logger log = Logger.getLogger(TomcatEnvironment.class);

    // FIXME: normally these should be per deployment.
    protected static final String[] replaceablePackages;

    private Map<ClassLoader, Set<String>> loadedClasses = new HashMap<>();

    static {
        String plist = AgentOptions.getOption(AgentOption.PACKAGES);
        if (plist == null || plist.length() == 0) {
            replaceablePackages = new String[0];
        } else {
            replaceablePackages = plist.split(";");
        }
    }

    @Override
    public boolean isClassReplaceable(String className, ClassLoader loader) {
        // if the classloader is not one of the webapps, we don't attempt to reload
        // it.

        if (loader == null) {
            log.trace("isClassReplaceable: " + className + " - false, loader is null");
            return false;
        }

        if ("org.apache.jasper.servlet.JasperLoader".equals(loader.getClass().getCanonicalName())) {
            loader = loader.getParent();
        }

        if (!"org.apache.catalina.loader.WebappClassLoader".equals(loader.getClass().getCanonicalName())) {
            log.trace("isClassReplaceable: " + className + " - false, loader is not WebappClassLoader");
            return false;
        }

        // if no packages are given, replace all classes.
        if (replaceablePackages.length == 0) {
            log.trace("isClassReplaceable: " + className + " - true. reloading all packages");
            addAlreadyLoadedClass(loader, className);
            return true;
        }

        // otherwise we check if the class is inside one of the replaceable
        // packages.
        for (String packageName : replaceablePackages) {
            if (className.startsWith(packageName)) {
                log.trace("isClassReplaceable: " + className + " - true. in package " + packageName);
                addAlreadyLoadedClass(loader, className);
                return true;
            }
        }

        if (className.contains("$Proxy")) {
            log.trace("isClassReplaceable: " + className + " - true. proxy class");
            addAlreadyLoadedClass(loader, className);
            return true;
        }

        log.trace("isClassReplaceable: " + className + " - false");
        return false;
    }

    public void recordTimestamp(String className, ClassLoader loader) {
        addAlreadyLoadedClass(loader, className);
        log.trace("recordTimestamp for " + className);
    }

    public ChangedClasses getUpdatedClasses(final String deploymentName, Map<String, Long> updatedClasses) {
        log.trace("getUpdatedClasses for " + deploymentName + " " + updatedClasses);
        if (updatedClasses == null || updatedClasses.isEmpty()) {
            return new ChangedClasses(Collections.emptySet(), Collections.emptySet(), null);
        }

        final Set<Class<?>> result = new HashSet<Class<?>>();
        final Set<String> newClasses;
        final Set<String> classesToReload;

        ClassLoader loader = getClassLoaderForDeployment(deploymentName);
        log.trace("Reloading classloader is " + loader);

        final Set<String> alreadyLoadedClasses = getAlreadyLoadedClasses(loader);

        newClasses = new HashSet<>(updatedClasses.keySet());
        newClasses.removeAll(alreadyLoadedClasses);

        classesToReload = new HashSet<>(updatedClasses.keySet());
        classesToReload.removeAll(newClasses);

        log.trace("classes to reload " + classesToReload);
        log.trace("new classes " + newClasses);

        for (String className : classesToReload) {
            try {
                result.add(loader.loadClass(className));
            } catch (ClassNotFoundException e) {
                log.error("Could not load class " + className, e);
            }
        }

        return new ChangedClasses(result, newClasses, loader);
    }

    private void addAlreadyLoadedClass(ClassLoader loader, String className) {
        Set<String> alreadyLoadedClasses = loadedClasses.get(loader);

        if (alreadyLoadedClasses == null) {
            alreadyLoadedClasses = new HashSet<>();
            loadedClasses.put(loader, alreadyLoadedClasses);
        }

        alreadyLoadedClasses.add(className);
    }

    private Set<String> getAlreadyLoadedClasses(ClassLoader loader) {
        Set<String> result = loadedClasses.get(loader);

        if (result == null) {
            result = new HashSet<>();
            loadedClasses.put(loader, result);
        }

        return result;
    }

    /**
     * We always update the full list of resources from the client.
     * @param deploymentName
     * @param updatedResources
     * @return
     */
    @Override
    public Set<String> getUpdatedResources(final String deploymentName,
                                           final Map<String, Long> updatedResources) {
        return updatedResources.keySet();
    }

    @Override
    public void updateResource(final String deploymentName,
                               final Map<String, byte[]> replacedResources) {
        String absolutePath = getAbsoluteDeploymentPath(deploymentName);

        log.trace("Absolute deployment path for " + deploymentName + " is " + absolutePath);

        for (Map.Entry<String, byte[]> entry: replacedResources.entrySet()) {
            Path target = Paths.get(absolutePath).resolve(entry.getKey());

            log.info("Reloading resource: " + entry.getKey() + " to " + target.toFile());

            try (FileOutputStream outputStream =
                         new FileOutputStream(target.toFile())) {
                outputStream.write(entry.getValue());
            } catch (IOException e) {
                log.error("Unable to update " + entry.getKey(), e);
            }
        }
    }

    private String getAbsoluteDeploymentPath(String deploymentName) {
        String contextName = "/" + deploymentName;

        Set<StandardContext> servletContexts = (Set<StandardContext>)
                InstanceTracker.get("org.apache.catalina.core.StandardContext");

        for (StandardContext context : servletContexts) {
            if (contextName.equals(context.getPath())) {
                return context.getRealPath("");
            }
        }

        return null;
    }

    /**
     *
     * Gets the classloader for the deployment.
     * @param deploymentName
     * @return
     */
    private ClassLoader getClassLoaderForDeployment(String deploymentName) {
        String contextName = "/" + deploymentName;

        Set<StandardContext> servletContexts = (Set<StandardContext>)
                InstanceTracker.get("org.apache.catalina.core.StandardContext");

        for (StandardContext context : servletContexts) {
            if (contextName.equals(context.getPath())) {
                return context.getLoader().getClassLoader();
            }
        }

        return null;
    }

}
