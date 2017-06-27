package org.fakereplace.client.standalone.embedded;

import org.fakereplace.client.ClassData;
import org.fakereplace.client.ResourceData;
import org.fakereplace.client.standalone.DataSender;
import org.fakereplace.client.standalone.DeploymentData;
import org.fakereplace.client.standalone.Util;
import org.fakereplace.client.standalone.config.DeploymentConfig;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Enqueues and processes data to be sent.
 */
public class DeferredDataSender implements DataSender {
    public static final int DEFAULT_TIMEOUT = 500;
    //    private final static Logger log = Logger.getLogger(DeferredDataSender.class);

    private Map<String, DeploymentData> deploymentDataMap = new LinkedHashMap<>();
    private Consumer<DeploymentData> deploymentDataConsumer;

    private Map<DeploymentData, TimeoutCall> timeoutCallMap = new WeakHashMap<>();

    public DeferredDataSender(Consumer<DeploymentData> deploymentDataConsumer) {
        this.deploymentDataConsumer = deploymentDataConsumer;
    }

    @Override
    public void sendChangedClass(DeploymentConfig deploymentConfig,
                                 File basePath,
                                 File file) {
        DeploymentData deploymentData;

        synchronized (this) {
            deploymentData = getDeploymentData(deploymentConfig);
            registerChangedClass(deploymentData, basePath, file);
        }

        getTimeoutCall(deploymentData)
                .reschedule(DEFAULT_TIMEOUT);
    }

    @Override
    public void removeChangedClass(DeploymentConfig deploymentConfig,
                                 File basePath,
                                 File file) {
        DeploymentData deploymentData;

        synchronized (this) {
            deploymentData = getDeploymentData(deploymentConfig);
            removeChangedClass(deploymentData, basePath, file);
        }

        getTimeoutCall(deploymentData)
                .reschedule(DEFAULT_TIMEOUT);
    }

    @Override
    public void sendChangedResource(DeploymentConfig deploymentConfig,
                                    File basePath,
                                    File file) {
        DeploymentData deploymentData;

        synchronized (this) {
            deploymentData = getDeploymentData(deploymentConfig);
            registerChangedResource(deploymentData, basePath, file);
        }

        getTimeoutCall(deploymentData)
                .reschedule(DEFAULT_TIMEOUT);
    }

    @Override
    public void removeChangedResource(DeploymentConfig deploymentConfig,
                                      File basePath,
                                      File file) {
        DeploymentData deploymentData;

        synchronized (this) {
            deploymentData = getDeploymentData(deploymentConfig);
            removeChangedResource(deploymentData, basePath, file);
        }

        getTimeoutCall(deploymentData)
                .reschedule(DEFAULT_TIMEOUT);
    }

    /**
     * Gets or creates the deployment data package to be sent.
     * @param deploymentConfig
     * @return
     */
    private DeploymentData getDeploymentData(DeploymentConfig deploymentConfig) {
        DeploymentData result = deploymentDataMap.get(deploymentConfig.getDeploymentName());

        if (result == null) {
            result = new DeploymentData(deploymentConfig);
            deploymentDataMap.put(deploymentConfig.getDeploymentName(), result);
        }

        return result;
    }

    private TimeoutCall getTimeoutCall(DeploymentData deploymentData) {
        TimeoutCall result = timeoutCallMap.get(deploymentData);

        if (result == null) {
            result = new TimeoutCall<>(
                    (deploymentData1) -> {
                        synchronized (DeferredDataSender.this) {
                            deploymentDataMap.remove(deploymentData.getDeploymentName());
                        }

                        deploymentDataConsumer.accept(deploymentData);
                    },
                    deploymentData,
                    DEFAULT_TIMEOUT);

            timeoutCallMap.put(deploymentData, result);
        }

        return result;
    }

    private void registerChangedClass(DeploymentData deploymentData, File basePath, File file) {
        final String relFile = file.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
        final String className = relFile.substring(0, relFile.length() - ".class".length()).replace(File.separator, ".");

        if (!isClassNameAllowed(deploymentData.getDeploymentConfig(), className)) {
            return; // ignore classes where the packages are filtered out.
        }

        long timestamp = file.lastModified();

        deploymentData.getClasses().put(
                className,
                new ClassData(className, timestamp, () -> Util.getBytesFromFile(file)));

    }

    private void removeChangedClass(DeploymentData deploymentData, File basePath, File file) {
        final String relFile = file.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
        final String className = relFile.substring(0, relFile.length() - ".class".length()).replace(File.separator, ".");

        if (!isClassNameAllowed(deploymentData.getDeploymentConfig(), className)) {
            return; // ignore classes where the packages are filtered out.
        }

        deploymentData.getClasses().remove(className);
    }

    /**
     * Notify a single changed file.
     * @param deploymentData
     * @param basePath
     * @param file
     */
    private void registerChangedResource(DeploymentData deploymentData, File basePath, File file) {
        final String relFile = file.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);
        long timestamp = file.lastModified();

        deploymentData.getResources().put(relFile,
                new ResourceData(relFile, timestamp, () -> Util.getBytesFromFile(file)));

    }

    /**
     * Remove a previously changed file.
     * @param deploymentData
     * @param basePath
     * @param file
     */
    private void removeChangedResource(DeploymentData deploymentData, File basePath, File file) {
        final String relFile = file.getAbsolutePath().substring(basePath.getAbsolutePath().length() + 1);

        deploymentData.getResources().remove(relFile);

    }

    /**
     * Gets the next available deployment data from the data map.
     * @return
     */
    public DeploymentData getNextDeploymentData() {
        String keyToReturn = deploymentDataMap.keySet().iterator().next();
        DeploymentData result = deploymentDataMap.get(keyToReturn);
        deploymentDataMap.remove(keyToReturn);

        return result;
    }


    /**
     * Check if the current class name, is allowed by the current deployment configuration
     * to be reloaded, in the filtered packages.
     *
     * If no filters are set, then all classes are available for reloading. Otherwise
     * check the actual filters.
     *
     * @param deploymentConfig
     * @param className
     * @return
     */
    private boolean isClassNameAllowed(DeploymentConfig deploymentConfig, String className) {
        if (deploymentConfig.getFilteredPackages().isEmpty()) {
            return true;
        }

        for (String filter : deploymentConfig.getFilteredPackages()) {
            if (filter.equals(className)) {
                return true;
            }

            if (className.startsWith(filter + ".")) {
                return true;
            }
        }

        return false;
    }
}
