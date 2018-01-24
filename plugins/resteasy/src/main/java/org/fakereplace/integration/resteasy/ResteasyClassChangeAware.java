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

package org.fakereplace.integration.resteasy;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.logging.Logger;
import javassist.bytecode.AnnotationsAttribute;

public class ResteasyClassChangeAware implements ClassChangeAware {

    private static final String RESOURCES = "resteasy.scanned.resources";
    private final Logger logger = Logger.getLogger(ResteasyClassChangeAware.class);

    @Override
    public void afterChange(final List<ChangedClass> changed, final List<NewClassData> added) {
        boolean requiresRestart = false;
        ClassLoader classLoader = null;
        for (final ChangedClass c : changed) {
            if (!c.getChangedAnnotationsByType(Path.class).isEmpty() ||
                    c.getChangedClass().isAnnotationPresent(Path.class)) {
                requiresRestart = true;
                classLoader = c.getChangedClass().getClassLoader();
                break;
            }
        }
        Set<String> addedResources = new HashSet<>();
        for(NewClassData add : added) {
            AnnotationsAttribute attribute = (AnnotationsAttribute) add.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            if(attribute == null) {
                continue;
            }
            if(attribute.getAnnotation(Path.class.getName()) != null) {
                addedResources.add(add.getClassName());
                requiresRestart = true;
            }
            if(classLoader == null) {
                classLoader = add.getClassLoader();
            }
        }
        if (requiresRestart) {

            for (final Object servlet : InstanceTracker.get(ResteasyExtension.SERVLET_DISPATCHER)) {
                try {
                    final ResteasyServletConfig config = (ResteasyServletConfig) servlet.getClass().getField(ResteasyTransformer.FIELD_NAME).get(servlet);
                    if(config.getClassLoader() == classLoader) {
                        StringBuilder res = new StringBuilder(config.getServletContext().getInitParameter(RESOURCES));
                        if(res != null) {
                            for(String add : addedResources) {
                                res.append(",").append(add);
                            }
                            ResteasyServletContext sc = new ResteasyServletContext(config.getServletContext());
                            sc.getInitParams().put(RESOURCES, res.toString());
                            config.setServletContext(sc);
                        }
                        final Set<String> doNotClear = (Set<String>) servlet.getClass().getField(ResteasyTransformer.PARAMETER_FIELD_NAME).get(servlet);
                        clearContext(config.getServletContext(), doNotClear);
                        final ClassLoader old = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(classLoader);
                        try {
                            servlet.getClass().getMethod("destroy").invoke(servlet);
                            servlet.getClass().getMethod("init", ServletConfig.class).invoke(servlet, config);
                        } finally {
                            Thread.currentThread().setContextClassLoader(old);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Could not restart RESTeasy", e);
                }
            }
            for (final Object filter : InstanceTracker.get(ResteasyExtension.FILTER_DISPATCHER)) {
                try {
                    final ResteasyFilterConfig config = (ResteasyFilterConfig) filter.getClass().getField(ResteasyTransformer.FIELD_NAME).get(filter);
                    if(config.getClassLoader() == classLoader) {

                        StringBuilder res = new StringBuilder((String) config.getServletContext().getAttribute(RESOURCES));
                        if(res != null) {
                            for(String add : addedResources) {
                                res.append(",").append(add);
                            }
                            ResteasyServletContext sc = new ResteasyServletContext(config.getServletContext());
                            sc.getInitParams().put(RESOURCES, res.toString());
                            config.setServletContext(sc);
                        }
                        final Set<String> doNotClear = (Set<String>) filter.getClass().getField(ResteasyTransformer.PARAMETER_FIELD_NAME).get(filter);
                        clearContext(config.getServletContext(), doNotClear);
                        final ClassLoader old = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(classLoader);
                        try {
                            filter.getClass().getMethod("destroy").invoke(filter);
                            filter.getClass().getMethod("init", FilterConfig.class).invoke(filter, config);
                        } finally {
                            Thread.currentThread().setContextClassLoader(old);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Could not restart RESTeasy", e);
                }
            }

        }

    }

    /**
     * Clear any resteasy stuff from the context
     *
     */
    private void clearContext(final ServletContext servletContext, final Set<String> doNotClear) {
        final Enumeration names = servletContext.getAttributeNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement().toString();
            if (name.startsWith("org.jboss.resteasy") && !doNotClear.contains(name)) {
                servletContext.removeAttribute(name);
            }
        }
    }
}
