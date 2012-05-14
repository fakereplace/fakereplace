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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fakereplace.api.Extension;
import org.fakereplace.transformation.FakereplaceTransformer;

public class ResteasyExtension implements Extension {

    public static final String FILTER_DISPATCHER = "org.jboss.resteasy.plugins.server.servlet.FilterDispatcher";
    public static final String SERVLET_DISPATCHER = "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher";

    public String getClassChangeAwareName() {
        return "org.fakereplace.integration.resteasy.ResteasyClassChangeAware";
    }

    public Set<String> getIntegrationTriggerClassNames() {
        return new HashSet<String>(Arrays.asList(new String[] {FILTER_DISPATCHER, SERVLET_DISPATCHER}));
    }

    public Set<String> getTrackedInstanceClassNames() {
        Set<String> ret = new HashSet<String>();
        ret.add(FILTER_DISPATCHER);
        ret.add(SERVLET_DISPATCHER);
        return ret;
    }

    public List<FakereplaceTransformer> getTransformers() {
        return Collections.<FakereplaceTransformer>singletonList(new ResteasyTransformer());
    }
}
