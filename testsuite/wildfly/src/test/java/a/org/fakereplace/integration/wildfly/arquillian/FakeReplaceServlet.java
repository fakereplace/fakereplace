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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fakereplace.core.Fakereplace;
import org.fakereplace.replacement.AddedClass;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Services;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;

/**
 * @author Stuart Douglas
 */
@WebServlet(urlPatterns = "/update")
public class FakeReplaceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectInputStream in = new ObjectInputStream(req.getInputStream());
            String deployment = in.readUTF();

            ServiceController<DeploymentUnit> deploymentService = deploymentService(deployment);

            Module module = deploymentService.getValue().getAttachment(Attachments.MODULE);
            int size = in.readInt();
            ClassDefinition[] definitions = new ClassDefinition[size];

            for (int i = 0; i < size; ++i) {
                String className = in.readUTF();
                byte[] data = (byte[]) in.readObject();
                Class clazz = module.getClassLoader().loadClass(className);
                ClassDefinition d = new ClassDefinition(clazz, data);
                definitions[i] = d;
            }
            size = in.readInt();
            AddedClass[] added = new AddedClass[size];

            for (int i = 0; i < size; ++i) {
                String className = in.readUTF();
                byte[] data = (byte[]) in.readObject();
                added[i] = new AddedClass(className, data, module.getClassLoader());
            }
            size = in.readInt();
            Map<String, byte[]> resources = new HashMap<>();
            for (int i = 0; i < size; ++i) {
                String resourceName = in.readUTF();
                byte[] data = (byte[]) in.readObject();
                resources.put(resourceName, data);
            }
            updateResource(resources, deploymentService);
            Fakereplace.redefine(definitions, added);
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }

    }


    private void updateResource(final Map<String, byte[]> replacedResources, ServiceController<DeploymentUnit> deploymentService) {
        if (deploymentService == null) {
            return;
        }
        final ModuleClassLoader loader = deploymentService.getValue().getAttachment(Attachments.MODULE).getClassLoader();
        if (loader == null) {
            return;
        }

        final DeploymentUnit deploymentUnit = deploymentService.getValue();
        final ResourceRoot root = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);

        for (final Map.Entry<String, byte[]> entry : replacedResources.entrySet()) {
            final VirtualFile file = root.getRoot().getChild(entry.getKey());
            try {
                final FileOutputStream stream = new FileOutputStream(file.getPhysicalFile(), false);
                try {
                    stream.write(entry.getValue());
                    stream.flush();
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ServiceController<DeploymentUnit> deploymentService(final String deploymentName) {
        ServiceController<DeploymentUnit> deploymentService = (ServiceController<DeploymentUnit>) CurrentServiceContainer.getServiceContainer().getService(Services.deploymentUnitName(deploymentName));
        if (deploymentService == null) {
            //now try for a sub deployment
            for (final ServiceName serviceName : CurrentServiceContainer.getServiceContainer().getServiceNames()) {
                if (Services.JBOSS_DEPLOYMENT_SUB_UNIT.isParentOf(serviceName)) {
                    final String[] parts = serviceName.toArray();
                    if (parts[parts.length - 1].equals(deploymentName)) {
                        deploymentService = (ServiceController<DeploymentUnit>) CurrentServiceContainer.getServiceContainer().getService(serviceName);
                        break;
                    }
                }
            }
        }
        return deploymentService;
    }

}
