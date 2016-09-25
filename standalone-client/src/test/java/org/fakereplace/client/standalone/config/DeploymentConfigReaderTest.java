package org.fakereplace.client.standalone.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeploymentConfigReaderTest {
    @Test
    public void testConfigurationReading() {
        List<DeploymentConfig> configuration = new DeploymentConfigReader("src/test/resources/simple-config.properties")
                    .readDeploymentConfig();

        assertNotNull("Configuration should exist src/test/resources/simple-config.properties",
                configuration);
        assertEquals("There should be only two deployments read from" +
                "src/test/resources/simple-config.properties.", 2, configuration.size());

        DeploymentConfig firstConfiguration = configuration.get(0);

        assertNotNull(firstConfiguration);
        assertEquals("mycoolwar", firstConfiguration.getDeploymentName());
        assertEquals(Arrays.asList(
                "/foo/moo/target/classes",
                "/boo/hoo/target/classes"),
                firstConfiguration.getClassesFolderLocations());
        assertEquals(Arrays.asList("/foo/moo/src/main/webapp"),
                firstConfiguration.getResourcesLocation());
        assertEquals(Collections.emptyList(),
                firstConfiguration.getFilteredPackages());

        DeploymentConfig secondConfiguration = configuration.get(1);

        assertNotNull(secondConfiguration);
        assertEquals("notsocool", secondConfiguration.getDeploymentName());
        assertEquals(Arrays.asList(
                "/uncool/target/classes",
                "/uncool/other/target/classes"),
                secondConfiguration.getClassesFolderLocations());
        assertEquals(Arrays.asList("/uncool/src/main/webapp"),
                secondConfiguration.getResourcesLocation());
        assertEquals(Arrays.asList("org.fakereplace.what", "org.fakereplace.else"),
                secondConfiguration.getFilteredPackages());
    }
}