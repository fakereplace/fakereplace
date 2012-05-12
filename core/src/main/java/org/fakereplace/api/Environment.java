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
package org.fakereplace.api;

import java.util.Map;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public interface Environment {

    boolean isClassReplaceable(final String className, final ClassLoader loader);

    void recordTimestamp(final String className, final ClassLoader loader);

    Set<Class> getUpdatedClasses(final String deploymentName, final Map<String, Long> updatedClasses);

    Set<String> getUpdatedResources(final String deploymentName, final Map<String, Long> updatedResources);

    void updateResource(final String archiveName, Map<String, byte[]> replacedResources);

    <T> T getService(Class<T> clazz);

}
