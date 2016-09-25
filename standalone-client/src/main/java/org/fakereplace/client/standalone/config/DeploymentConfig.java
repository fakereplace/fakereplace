package org.fakereplace.client.standalone.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A deployment configuration section.
 *
 * It contains only a single set of folders for classes, resources and
 * filtered packages.
 *
 * <ul>
 *    <li>The folders are the actual folder paths from the disk to where the
 *        .class files are getting compiled.</li>
 *    <li>Resources are the actual folder paths from
 *        the disk where the resources should be read from.</li>
 *    <li>Filtered packages, is an optional list that when present will update
 *        only classes that belong under the given package list, for optimization
 *        reasons.</li>
 * </ul>
 *
 */
public class DeploymentConfig {
    private final String deploymentName;

    /**
     * A list of folders on the disk where the classes should be loaded from.
     */
    private List<String> classesFolderLocations = new ArrayList<String>();

    /**
     * A list of folders on the disk where classpath resources should be loaded
     * from.
      */
    private List<String> resourcesLocation = new ArrayList<String>();

    /**
     * A list of packages for which the update of the classes is permitted.
     * If missing or empty list, then all classes are permitted.
     */
    private List<String> filteredPackages = new ArrayList<String>();

    public DeploymentConfig(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public DeploymentConfig addMonitoredClasspathFolder(String folder) {
        classesFolderLocations.add(folder);

        return this;
    }

    public DeploymentConfig addMonitoredResourcesFolder(String folder) {
        resourcesLocation.add(folder);

        return this;
    }

    public DeploymentConfig addFilteredPackage(String packageName) {
        filteredPackages.add(packageName);

        return this;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public List<String> getClassesFolderLocations() {
        return classesFolderLocations;
    }

    public void setClassesFolderLocations(List<String> classesFolderLocations) {
        this.classesFolderLocations = classesFolderLocations;
    }

    public List<String> getResourcesLocation() {
        return resourcesLocation;
    }

    public void setResourcesLocation(List<String> resourcesLocation) {
        this.resourcesLocation = resourcesLocation;
    }

    public List<String> getFilteredPackages() {
        return filteredPackages;
    }

    public void setFilteredPackages(List<String> filteredPackages) {
        this.filteredPackages = filteredPackages;
    }
}
