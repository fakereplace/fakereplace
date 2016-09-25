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

package org.fakereplace.integration.tomcat;

import org.fakereplace.api.Extension;
import org.fakereplace.transformation.FakereplaceTransformer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bodan Mustiata &lt;bogdan.mustiata@gmail.com&gt;
 */
public class TomcatExtension implements Extension {

    public static final String RESOURCE_CACHE_CLASS = "org.apache.naming.resources.ResourceCache";
    public static final String TOMCAT_CONTEXT = "org.apache.catalina.core.StandardContext";

    private static final String CLASS_CHANGE_AWARE = "org.fakereplace.integration.tomcat.TomcatClassChangeAware";
    public static final String TOMCAT_ENVIRONMENT = "org.fakereplace.integration.tomcat.TomcatEnvironment";

    @Override
    public String getClassChangeAwareName() {
        return CLASS_CHANGE_AWARE;
    }

    @Override
    public Set<String> getIntegrationTriggerClassNames() {
        return Collections.singleton("org.apache.catalina.core.ContainerBase");
    }

    @Override
    public String getEnvironment() {
        return TOMCAT_ENVIRONMENT;
    }

    @Override
    public Set<String> getTrackedInstanceClassNames() {
        return new HashSet<>(Arrays.asList(new String[]{
                RESOURCE_CACHE_CLASS,
                TOMCAT_CONTEXT
        }));
    }

    @Override
    public List<FakereplaceTransformer> getTransformers() {
        return Collections.emptyList();
    }

}
