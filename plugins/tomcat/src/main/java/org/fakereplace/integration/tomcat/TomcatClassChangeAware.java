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

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.logging.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * @author Bogdan Mustiata &lt;bogdan.mustiata@gmail.com&gt;
 */
public class TomcatClassChangeAware implements ClassChangeAware {

    private static final Logger log = Logger.getLogger(TomcatClassChangeAware.class);

    @Override
    public void afterChange(List<ChangedClass> changed, List<NewClassData> added) {
        clearJSRResourceCache();
    }

    private void clearJSRResourceCache() {
        final Set<?> caches = InstanceTracker.get(TomcatExtension.RESOURCE_CACHE_CLASS);
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
