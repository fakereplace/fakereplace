package org.fakereplace.client.standalone.embedded;

import org.fakereplace.client.FakeReplaceClient;
import org.fakereplace.client.standalone.DataSender;
import org.fakereplace.client.standalone.DeploymentData;
import org.fakereplace.client.standalone.config.DeploymentConfig;
import org.fakereplace.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Takes care of sending the resource and class updates to
 * the fakereplace server asynchronously.
 */
public class FakeReplaceDataSender implements DataSender {

    private static final Logger log = Logger.getLogger(FakeReplaceDataSender.class);
    private final DeferredDataSender dataSender;

    public FakeReplaceDataSender() {
        dataSender = new DeferredDataSender(this::sendData);
    }

    @Override
    public void sendChangedClass(DeploymentConfig deploymentConfig,
                                 File basePath,
                                 File file) {
        dataSender.sendChangedClass(deploymentConfig, basePath, file);
    }

    @Override
    public void sendChangedResource(DeploymentConfig deploymentConfig,
                                    File basePath,
                                    File file) {
        dataSender.sendChangedResource(deploymentConfig, basePath, file);
    }

    public void sendData(DeploymentData deploymentData) {
        try {
            FakeReplaceClient.run(
                    deploymentData.getDeploymentName(),
                    deploymentData.getClasses(),
                    deploymentData.getResources());
        } catch (IOException e) {
            log.error("Unable to publish data to FakeReplace.", e);
        }
    }
}
