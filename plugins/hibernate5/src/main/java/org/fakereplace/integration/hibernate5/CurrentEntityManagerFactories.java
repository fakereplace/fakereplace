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

package org.fakereplace.integration.hibernate5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Stuart Douglas
 */
public class CurrentEntityManagerFactories {

    private static final Set<FakereplaceEntityManagerFactoryProxy> ENTITY_MANAGERS = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    public static void registerEntityManager(FakereplaceEntityManagerFactoryProxy proxy) {
        ENTITY_MANAGERS.add(proxy);
    }

    public static List<FakereplaceEntityManagerFactoryProxy> getEMFForEntities(final Set<Class<?>> changedClasses) {
        final List<FakereplaceEntityManagerFactoryProxy> ret = new ArrayList<FakereplaceEntityManagerFactoryProxy>();
        for (FakereplaceEntityManagerFactoryProxy entityManagerFactory : ENTITY_MANAGERS) {
            if(entityManagerFactory.containsEntity(changedClasses)) {
                ret.add(entityManagerFactory);
            }
        }
        return ret;
    }

    public static void removeEmf(FakereplaceEntityManagerFactoryProxy build) {
        ENTITY_MANAGERS.remove(build);
    }
}
