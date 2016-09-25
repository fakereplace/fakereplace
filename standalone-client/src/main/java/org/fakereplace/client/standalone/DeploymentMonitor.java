package org.fakereplace.client.standalone;

import org.fakereplace.client.standalone.config.DeploymentConfig;
import org.fakereplace.util.WatchServiceFileSystemWatcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.fakereplace.util.WatchServiceFileSystemWatcher.FileChangeEvent.Type.ADDED;
import static org.fakereplace.util.WatchServiceFileSystemWatcher.FileChangeEvent.Type.MODIFIED;

public class DeploymentMonitor {
    private final List<DeploymentConfig> deploymentConfigList;

    private DataSender dataSender;
    private WatchServiceFileSystemWatcher watcher;

    public DeploymentMonitor(List<DeploymentConfig> deploymentConfigList,
                             DataSender dataSender) {
        this.deploymentConfigList = deploymentConfigList;
        this.dataSender = dataSender;
    }


    public void startMonitoring() {
        // When instantiating a new watcher, this will automatically
        // create a new thread.
        watcher = new WatchServiceFileSystemWatcher();

        for (DeploymentConfig deploymentConfig : deploymentConfigList) {
            deploymentConfig.getClassesFolderLocations().stream()
                    .map(File::new)
                    .forEach(this.createClassMonitoring(watcher, deploymentConfig));

            deploymentConfig.getResourcesLocation().stream()
                    .map(File::new)
                    .forEach(this.createResourceMonitoring(watcher, deploymentConfig));
        }
    }

    public void stopMonitoring() {
        try {
            if (watcher == null) {
                throw new NullPointerException("The watcher was not yet started.");
            }

            watcher.close();
            watcher = null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Consumer<File> createResourceMonitoring(WatchServiceFileSystemWatcher watcher,
                                                    DeploymentConfig deploymentConfig) {
        return (File basePath) -> {
            watcher.watchPath(basePath, changes -> {
                changes.stream().forEach((e) -> {
                    if (e.getType() == ADDED || e.getType() == MODIFIED) {
                        dataSender.sendChangedResource(
                                deploymentConfig,
                                basePath,
                                e.getFile());
                    }
                });
            });
        };
    }

    /**
     * Handle changes for a single class change.
     *
     * @param watcher
     * @param deploymentConfig
     * @return
     */
    private Consumer<File> createClassMonitoring(WatchServiceFileSystemWatcher watcher,
                                                 DeploymentConfig deploymentConfig) {
        return (File basePath) -> {
            watcher.watchPath(basePath, changes -> {
                changes.stream().forEach((e) -> {
                    if (e.getType() == ADDED || e.getType() == MODIFIED) {
                        dataSender.sendChangedClass(
                                deploymentConfig,
                                basePath,
                                e.getFile());
                    }
                });
            });
        };
    }
}
