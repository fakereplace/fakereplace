package org.fakereplace.client.standalone;

import org.fakereplace.client.standalone.config.DeploymentConfig;

import java.io.File;

public interface DataSender {
    void sendChangedClass(DeploymentConfig deploymentConfig,
                          File basePath,
                          File file);

    void removeChangedClass(DeploymentConfig deploymentConfig,
                            File basePath,
                            File file);

    void sendChangedResource(DeploymentConfig deploymentConfig,
                             File basePath,
                             File file);

    void removeChangedResource(DeploymentConfig deploymentConfig,
                               File basePath,
                               File file);
}
