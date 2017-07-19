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

package org.fakereplace.integration.wildfly.autoupdate;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.fakereplace.core.ClassLookupManager;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.as.server.deployment.DeploymentCompleteServiceProcessor;
import org.jboss.as.server.deployment.Services;
import org.jboss.as.server.moduleservice.ServiceModuleLoader;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.RedirectBuilder;

/**
 * @author Stuart Douglas
 */
public class WebUpdateHandlerWrapper implements HandlerWrapper {

    private static final int UPDATE_INTERVAL = 1000;

    private static final String DEPLOYMENT = "deployment.";

    private static final String QUERY_STRING = "fakereplaceRestartCount"; //query string used to force a reload

    public static final HandlerWrapper INSTANCE = new WebUpdateHandlerWrapper();

    @Override
    public HttpHandler wrap(HttpHandler httpHandler) {
        return new WebUpdateHandler(httpHandler);
    }

    public static class WebUpdateHandler implements HttpHandler {
        private final HttpHandler httpHandler;
        private final AtomicLong nextUpdate = new AtomicLong();

        public WebUpdateHandler(HttpHandler httpHandler) {
            this.httpHandler = httpHandler;
        }

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            long next = nextUpdate.get();
            if (System.currentTimeMillis() > next) {
                if (nextUpdate.compareAndSet(next, System.currentTimeMillis() + UPDATE_INTERVAL)) {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    if (contextClassLoader instanceof ModuleClassLoader) {
                        if (WildflyAutoUpdate.runUpdate((ModuleClassLoader) contextClassLoader) == WildflyAutoUpdate.Result.REDEPLOY_REQUIRED) {
                            if (handleRedeployment((ModuleClassLoader) contextClassLoader, httpServerExchange)) {
                                return;
                            }
                        }
                    }
                }
            }


            httpHandler.handleRequest(httpServerExchange);
        }

        private boolean handleRedeployment(ModuleClassLoader classLoader, HttpServerExchange exchange) {
            ServiceContainer container = CurrentServiceContainer.getServiceContainer();

            String deploymentName = classLoader.getModule().getIdentifier().getName();
            if (deploymentName.startsWith(DEPLOYMENT)) {
                deploymentName = deploymentName.substring(DEPLOYMENT.length());
            }
            ServiceName serviceName = Services.JBOSS_DEPLOYMENT_UNIT.append(deploymentName);
            ServiceController<?> controller = container.getService(serviceName);
            if (controller == null) {
                System.out.println("Failed to restart deployment");
                return false;
            }
            final CountDownLatch latch = new CountDownLatch(1);
            controller.addListener(new AbstractServiceListener<Object>() {

                @Override
                public void transition(ServiceController<?> controller, ServiceController.Transition transition) {

                    if (transition.getAfter().getState() == ServiceController.State.DOWN) {
                        final ServiceName moduleServiceName = ServiceModuleLoader.moduleServiceName(classLoader.getModule().getIdentifier());
                        container.addListener(new AbstractServiceListener<Object>() {
                            @Override
                            public void listenerAdded(ServiceController<?> controller) {
                                if (!moduleServiceName.equals(controller.getName())) {
                                    controller.removeListener(this);
                                }
                            }

                            @Override
                            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                                if (transition.getAfter().getState() == ServiceController.State.UP) {
                                    controller.removeListener(this);
                                    Map<String, byte[]> data = WildflyAutoUpdate.changedClassDataForLoader(classLoader);
                                    if (data != null) {
                                        System.out.println(data);
                                        for (Map.Entry<String, byte[]> entry : data.entrySet()) {
                                            ClassLookupManager.addClassInfo(entry.getKey().replace("/", "."), ((Module) controller.getValue()).getClassLoader(), entry.getValue());
                                        }
                                    }

                                }
                            }
                        });
                        final ServiceName deploymentCompleteName = controller.getName().append(DeploymentCompleteServiceProcessor.SERVICE_NAME);
                        container.addListener(new AbstractServiceListener<Object>() {
                            @Override
                            public void listenerAdded(ServiceController<?> controller) {
                                if(!deploymentCompleteName.equals(controller.getName())) {
                                    controller.removeListener(this);
                                }
                            }

                            @Override
                            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                                if(transition.entersRestState()) {
                                    latch.countDown();
                                }
                            }
                        });


                        controller.setMode(ServiceController.Mode.ACTIVE);
                        controller.removeListener(this);
                    }
                }
            });
            controller.setMode(ServiceController.Mode.NEVER);
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String count = "1";
            if (exchange.getQueryParameters().containsKey(QUERY_STRING)) {
                try {
                    count = Integer.toString(Integer.parseInt(exchange.getQueryParameters().get(QUERY_STRING).getFirst()) + 1);
                } catch (Exception e) {
                    //ignore
                }
            }
            ArrayDeque<String> value = new ArrayDeque<>();
            exchange.getQueryParameters().put(QUERY_STRING, value);
            value.addLast(count);
            exchange.getResponseHeaders().put(Headers.LOCATION, RedirectBuilder.redirect(exchange, exchange.getRelativePath(), true));
            exchange.setStatusCode(302);
            return true;
        }
    }
}
