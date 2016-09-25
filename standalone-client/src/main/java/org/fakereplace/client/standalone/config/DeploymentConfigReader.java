package org.fakereplace.client.standalone.config;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Read a configuration file on what folders to monitor for reloading.
 */
public class DeploymentConfigReader {
    private String configFileName = "fakereplace.properties";

    public DeploymentConfigReader() {
    }

    public DeploymentConfigReader(String configFileName) {
        this.configFileName = configFileName;
    }

    public List<DeploymentConfig> readDeploymentConfig() {
        Properties propertiesFile = readConfigFile(configFileName);
        List<DeploymentConfig> result = new ArrayList<DeploymentConfig>();

        if (propertiesFile == null) {
            return null;
        }

        String deploymentsProperty = propertiesFile.getProperty("deployments", "");

        for (String deploymentName : readList(deploymentsProperty)) {
            DeploymentConfig deploymentConfig = new DeploymentConfig(deploymentName);

            List<String> classes = readList(propertiesFile.getProperty(deploymentName + ".classes", "") );
            deploymentConfig.setClassesFolderLocations( classes );

            List<String> resources = readList(propertiesFile.getProperty(deploymentName + ".resources", "") );
            deploymentConfig.setResourcesLocation(resources);

            List<String> filteredPackages = readList(propertiesFile.getProperty(deploymentName + ".packages", "") );
            deploymentConfig.setFilteredPackages(filteredPackages);

            result.add(deploymentConfig);
        }

        return result;
    }

    private List<String> readList(String entry) {
        if (entry.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();

        for (String value : entry.split("\\s*,\\s*")) {
            if (!value.trim().isEmpty()) {
                result.add(value);
            }
        }

        return result;
    }

    private Properties readConfigFile(String fileName) {
        File configurationFile = new File(fileName);

        if (!configurationFile.exists()) {
            return null;
        }

        System.out.println("Reading configuration from: " + configurationFile.getAbsolutePath());

        try (Reader data = new FileReader(configurationFile)) {
            Properties properties = new Properties();
            properties.load(data);

            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read config: " + fileName, e);
        }
    }
}
