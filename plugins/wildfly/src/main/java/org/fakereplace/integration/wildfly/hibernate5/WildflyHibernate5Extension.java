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

package org.fakereplace.integration.wildfly.hibernate5;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fakereplace.core.FakereplaceTransformer;
import org.fakereplace.core.InternalExtension;

public class WildflyHibernate5Extension implements InternalExtension {

    static final String PERSISTENCE_UNIT_SERVICE = "org.jboss.as.jpa.service.PersistenceUnitServiceImpl";
    static final String PERSISTENCE_PHASE_ONE_SERVICE = "org.jboss.as.jpa.service.PhaseOnePersistenceUnitServiceImpl";

    private static final String CLASS_CHANGE_AWARE = "org.fakereplace.integration.wildfly.hibernate5.WildflyHibernate5ClassChangeAware";

    @Override
    public String getClassChangeAwareName() {
        return CLASS_CHANGE_AWARE;
    }

    @Override
    public Set<String> getIntegrationTriggerClassNames() {
        return Collections.singleton(PERSISTENCE_UNIT_SERVICE);
    }

    @Override
    public Set<String> getTrackedInstanceClassNames() {
        return new HashSet<>(Arrays.asList(new String[]{PERSISTENCE_UNIT_SERVICE, PERSISTENCE_PHASE_ONE_SERVICE}));
    }

    @Override
    public List<FakereplaceTransformer> getTransformers() {
        return Collections.singletonList(new WildflyHibernate5ClassTransformer());
    }

}
