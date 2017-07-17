/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package a.org.fakereplace.integration.wildfly.arquillian;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class FakeReplaceDeploymentProvider {

    private static final Logger log = Logger.getLogger(FakeReplaceDeploymentProvider.class);

    private volatile Archive<?> archive = null;

    public synchronized void doServiceDeploy(@Observes BeforeDeploy event, Container container) {

        // only deploy the service if the deployment has been enriched by the jmx-as7 protocol
        if (archive == null) {
            WebArchive serviceArchive = ShrinkWrap.create(WebArchive.class, "fakereplace.war");
            serviceArchive.addClass(FakeReplaceServlet.class);
            serviceArchive.setManifest(new StringAsset("Dependencies: org.jboss.modules,org.jboss.msc,org.jboss.as.server\r\n"));
            try {
                DeployableContainer<?> deployableContainer = container.getDeployableContainer();
                deployableContainer.deploy(serviceArchive);
                archive = serviceArchive;
            } catch (Throwable th) {
                log.error("Cannot deploy fakereplace service", th);
            }
        }
    }

    public synchronized void undeploy(@Observes BeforeStop event, Container container) {
        if (archive == null) {
            return;
        }
        try {
            DeployableContainer<?> deployableContainer = container.getDeployableContainer();
            deployableContainer.undeploy(archive);
            archive = null;
        } catch (Throwable th) {
            log.error("Cannot undeploy fakereplace service", th);
        }
    }
}

