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
package org.fakereplace.integration.wildfly;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.logging.Logger;

/**
 * @author Stuart Douglas
 */
public class WildflyClassChangeAware implements ClassChangeAware {

    private static final Logger log = Logger.getLogger(WildflyClassChangeAware.class);

    @Override
    public void afterChange(List<ChangedClass> changed, List<NewClassData> added) {
        clearJSRResourceCache();
    }

    private void clearJSRResourceCache() {
        final Set<?> caches = InstanceTracker.get(WildflyExtension.RESOURCE_CACHE_CLASS);
        for(Object cache : caches) {
            try {
                Field field = cache.getClass().getDeclaredField("cache");
                field.setAccessible(true);
                final Class fieldType = field.getType();
                field.set(cache, Array.newInstance(fieldType.getComponentType(), 0));
            } catch (Exception e) {
                log.error("Failed to clear JSF resource cache", e);
                e.printStackTrace();
            }
        }
    }

}
