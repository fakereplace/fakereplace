/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.integration.resteasy;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.data.InstanceTracker;

public class ResteasyClassChangeAware implements ClassChangeAware {

    @Override
    public void beforeChange(final List<Class<?>> changed, final List<ClassIdentifier> added) {

    }

    @Override
    public void afterChange(final List<ChangedClass> changed, final List<ClassIdentifier> added) {
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

        if (requiresRestart) {

            try {
                for (final Object servlet : InstanceTracker.get(ResteasyExtension.SERVLET_DISPATCHER)) {
                    final ServletConfig config = (ServletConfig) servlet.getClass().getField(ResteasyTransformer.FIELD_NAME).get(servlet);
                    final Set<String> doNoyClear = (Set<String>) servlet.getClass().getField(ResteasyTransformer.PARAMETER_FIELD_NAME).get(servlet);
                    clearContext(config.getServletContext(), doNoyClear);
                    final ClassLoader old = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(classLoader);
                    try {
                        servlet.getClass().getMethod("destroy").invoke(servlet);
                        servlet.getClass().getMethod("init", ServletConfig.class).invoke(servlet, config);
                    } finally {
                        Thread.currentThread().setContextClassLoader(old);
                    }
                }
                for (final Object filter : InstanceTracker.get(ResteasyExtension.FILTER_DISPATCHER)) {
                    final FilterConfig config = (FilterConfig) filter.getClass().getField(ResteasyTransformer.FIELD_NAME).get(filter);
                    final Set<String> doNoyClear = (Set<String>) filter.getClass().getField(ResteasyTransformer.PARAMETER_FIELD_NAME).get(filter);
                    clearContext(config.getServletContext(), doNoyClear);
                    final ClassLoader old = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(classLoader);
                    try {
                        filter.getClass().getMethod("destroy").invoke(filter);
                        filter.getClass().getMethod("init", FilterConfig.class).invoke(filter, config);
                    } finally {
                        Thread.currentThread().setContextClassLoader(old);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Clear any resteasy stuff from the context
     *
     * @param servletContext
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
