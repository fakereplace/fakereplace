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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fakereplace.core.FakereplaceTransformer;
import org.fakereplace.core.InternalExtension;

public class ResteasyExtension implements InternalExtension {

    static final String FILTER_DISPATCHER = "org.jboss.resteasy.plugins.server.servlet.FilterDispatcher";
    static final String SERVLET_DISPATCHER = "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher";

    @Override
    public String getClassChangeAwareName() {
        return "org.fakereplace.integration.resteasy.ResteasyClassChangeAware";
    }

    @Override
    public Set<String> getIntegrationTriggerClassNames() {
        return new HashSet<String>(Arrays.asList(new String[]{FILTER_DISPATCHER, SERVLET_DISPATCHER}));
    }

    @Override
    public Set<String> getTrackedInstanceClassNames() {
        Set<String> ret = new HashSet<String>();
        ret.add(FILTER_DISPATCHER);
        ret.add(SERVLET_DISPATCHER);
        return ret;
    }

    @Override
    public List<FakereplaceTransformer> getTransformers() {
        return Collections.singletonList(new ResteasyTransformer());
    }
}
