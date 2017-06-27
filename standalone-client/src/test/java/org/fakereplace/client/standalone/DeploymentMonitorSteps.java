package org.fakereplace.client.standalone;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.fakereplace.client.standalone.config.DeploymentConfig;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Tests if the monitoring of resources and classes
 * works as expected.
 */
public class DeploymentMonitorSteps {
    private static DeploymentMonitor deploymentMonitor;
    private static TestDataSender testDataSender;

    @After
    public static void shutdownMonitoring() throws IOException {
        stopMonitoring();
        removeDefaultFolderStructure();
    }

    @Given("^I monitor the 'com\\.monitored' package in the default test folder$")
    public void i_monitor_the_com_monitored_package_in_the_default_test_folder() throws Throwable {
        copyDefaultFolderStructure();
        startMonitoring("com.monitored");
    }

    @Given("^I monitor the default test folder with no package restrictions$")
    public void i_monitor_the_default_test_folder_with_no_package_restrictions() throws Throwable {
        copyDefaultFolderStructure();
        startMonitoring(/* package restriction */ null);
    }

    @When("^I change two resources, one in the monitored folder, the other outside$")
    public void i_change_two_resources_one_in_the_monitored_folder_the_other_outside() throws Throwable {
        FileUtils.copyFile(
                new File("src/test/monitored-default/resources/monitored/monitored.txt"),
                new File("target/monitored-default/resources/monitored/monitored.txt"));
        FileUtils.copyFile(
                new File("src/test/monitored-default/resources/unmonitored/unmonitored.txt"),
                new File("target/monitored-default/resources/unmonitored/unmonitored.txt"));
    }

    @When("^I change a class inside the monitored folder, in the monitored package$")
    public void i_change_a_class_inside_a_monitored_folder_in_a_monitored_package() throws Throwable {
        FileUtils.copyFile(
                new File("src/test/monitored-default/classpath/monitored/com/monitored/a/A.class"),
                new File("target/monitored-default/classpath/monitored/com/monitored/a/A.class"));
    }

    @When("^I change a class inside the monitored folder, in a package that is not monitored$")
    public void i_change_a_class_inside_a_monitored_folder_in_a_package_that_is_not_monitored() throws Throwable {
        FileUtils.copyFile(
                new File("src/test/monitored-default/classpath/monitored/com/unmonitored/a/B.class"),
                new File("target/monitored-default/classpath/monitored/com/unmonitored/a/B.class"));
    }

    @When("^I change a class outside a monitored folder$")
    public void i_change_a_class_outside_a_monitored_folder() throws Throwable {
        FileUtils.copyFile(
                new File("src/test/monitored-default/classpath/unmonitored/com/monitored/a/C.class"),
                new File("target/monitored-default/classpath/unmonitored/com/monitored/a/C.class"));
    }

    @Then("^I get one changed monitored class$")
    public void i_get_one_changed_monitored_class() throws Throwable {
        Thread.sleep(1000);
        assertEquals(1, testDataSender.getDeploymentData().getClasses().size());

        assertEquals(Collections.singleton("com.monitored.a.A"),
                testDataSender.getDeploymentData().getClasses().keySet());
    }

    @Then("^I get two changed monitored classes$")
    public void i_get_two_changed_monitored_class() throws Throwable {
        Thread.sleep(1000);
        assertEquals(2, testDataSender.getDeploymentData().getClasses().size());

        assertEquals(new HashSet<>(Arrays.asList("com.monitored.a.A", "com.unmonitored.a.B")),
                testDataSender.getDeploymentData().getClasses().keySet());
    }

    @Then("^I get one changed monitored resource$")
    public void i_get_one_changed_monitored_resource() throws Throwable {
        Thread.sleep(1000);
        assertEquals(1, testDataSender.getDeploymentData().getResources().size());

        assertEquals(Collections.singleton("monitored.txt"),
                testDataSender.getDeploymentData().getResources().keySet());
    }

    private void copyDefaultFolderStructure() throws IOException {
        FileUtils.copyDirectoryToDirectory(
                new File("src/test/monitored-default"),
                new File("target/"));
    }

    private static void removeDefaultFolderStructure() throws IOException {
        FileUtils.forceDelete(new File("target/monitored-default"));
    }

    private static void stopMonitoring() {
        if (deploymentMonitor != null) {
            deploymentMonitor.stopMonitoring();
        }
        deploymentMonitor = null;
        testDataSender = null;
    }

    private void startMonitoring(String filteringPackage) {
        testDataSender = new TestDataSender();

        DeploymentConfig deploymentConfig = new DeploymentConfig("fakeapp")
                .addMonitoredClasspathFolder("target/monitored-default/classpath/monitored")
                .addMonitoredResourcesFolder("target/monitored-default/resources/monitored");

        if (filteringPackage != null) {
            deploymentConfig = deploymentConfig.addFilteredPackage(filteringPackage);
        }

        deploymentMonitor = new DeploymentMonitor(
                Collections.singletonList(deploymentConfig),
                testDataSender);

        deploymentMonitor.startMonitoring();
    }
}
