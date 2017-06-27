package org.fakereplace.client.standalone;

import org.fakereplace.client.ClassData;
import org.fakereplace.client.ResourceData;
import org.fakereplace.client.standalone.config.DeploymentConfig;

import java.util.HashMap;
import java.util.Map;

public class DeploymentData {
    private DeploymentConfig deploymentConfig;
    private Map<String, ClassData> classes;
    private Map<String, ResourceData> resources;

    public DeploymentData(DeploymentConfig deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
        this.classes = new HashMap<>();
        this.resources = new HashMap<>();
    }

    public Map<String, ClassData> getClasses() {
        return classes;
    }

    public Map<String, ResourceData> getResources() {
        return resources;
    }

    public DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    public String getDeploymentName() {
        return deploymentConfig.getDeploymentName();
    }
}
