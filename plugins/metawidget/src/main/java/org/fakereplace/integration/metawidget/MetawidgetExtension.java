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

package org.fakereplace.integration.metawidget;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fakereplace.Extension;

public class MetawidgetExtension implements Extension {

    static final String BASE_PROPERTY_STYLE = "org.metawidget.inspector.impl.propertystyle.BasePropertyStyle";
    static final String BASE_ACTION_STYLE = "org.metawidget.inspector.impl.actionstyle.BaseActionStyle";

    private static final Set<String> classNames;

    static {
        Set<String> ret = new HashSet<>();
        ret.add(BASE_ACTION_STYLE);
        ret.add(BASE_PROPERTY_STYLE);
        classNames = Collections.unmodifiableSet(ret);
    }

    @Override
    public String getClassChangeAwareName() {
        return "org.fakereplace.integration.metawidget.MetawidgetClassChangeAware";
    }

    @Override
    public Set<String> getIntegrationTriggerClassNames() {
        return classNames;
    }

    @Override
    public Set<String> getTrackedInstanceClassNames() {
        return classNames;
    }

}
