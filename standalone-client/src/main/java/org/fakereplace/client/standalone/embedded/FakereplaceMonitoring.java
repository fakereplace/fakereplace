package org.fakereplace.client.standalone.embedded;

import org.fakereplace.client.standalone.DeploymentMonitor;
import org.fakereplace.client.standalone.config.DeploymentConfig;
import org.fakereplace.client.standalone.config.DeploymentConfigReader;
import org.fakereplace.logging.Logger;

import java.util.List;

public class FakereplaceMonitoring {
    private List<DeploymentConfig> deploymentConfigList;
    private static final Logger log = Logger.getLogger(FakereplaceMonitoring.class);

    public void startMonitoring() {
        try {
            deploymentConfigList = readDeploymentConfiguration();

            if (deploymentConfigList == null) {
                log.info("Configuration file doesn't exists. Won't start a fakereplace monitoring thread.");
                return;
            }

            DeploymentMonitor deploymentMonitor = new DeploymentMonitor(
                    deploymentConfigList,
                    new FakeReplaceDataSender()
            );

            deploymentMonitor.startMonitoring();
        } catch (Exception e) {
            log.error("Unable to load the monitoring thread:", e);
        }
    }

    private List<DeploymentConfig> readDeploymentConfiguration() {
        return new DeploymentConfigReader().readDeploymentConfig();
    }
}
