package org.fakereplace.client.standalone;

import org.fakereplace.client.standalone.config.DeploymentConfig;
import org.fakereplace.client.standalone.embedded.DeferredDataSender;

import java.io.File;

public class TestDataSender implements DataSender {
    private final DeferredDataSender dataSender;
    private DeploymentData deploymentData;

    public TestDataSender() {
        this.dataSender = new DeferredDataSender(this::sendData);
    }

    @Override
    public void sendChangedClass(DeploymentConfig deploymentConfig, File basePath, File file) {
        dataSender.sendChangedClass(deploymentConfig, basePath, file);
    }

    @Override
    public void removeChangedClass(DeploymentConfig deploymentConfig, File basePath, File file) {
        dataSender.removeChangedClass(deploymentConfig, basePath, file);
    }

    @Override
    public void sendChangedResource(DeploymentConfig deploymentConfig, File basePath, File file) {
        dataSender.sendChangedResource(deploymentConfig, basePath, file);
    }

    @Override
    public void removeChangedResource(DeploymentConfig deploymentConfig, File basePath, File file) {
        dataSender.removeChangedResource(deploymentConfig, basePath, file);
    }

    public void sendData(DeploymentData deploymentData) {
        this.deploymentData = deploymentData;
    }

    public DeploymentData getDeploymentData() {
        return deploymentData;
    }
}
