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

package org.fakereplace.integration.hibernate4;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fakereplace.api.Attachments;
import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.environment.CurrentEnvironment;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.hibernate4.HibernateEnvironment;

/**
 * @author Stuart Douglas
 */
public class Hibernate4ClassChangeAware implements ClassChangeAware {
    @Override
    public void beforeChange(final List<Class<?>> changed, final List<ClassIdentifier> added, final Attachments attachments) {

    }

    @Override
    public void afterChange(final List<ChangedClass> changed, final List<ClassIdentifier> added, final Attachments attachments) {
        final Set<Class<?>> changedClasses = new HashSet<Class<?>>();
        for(ChangedClass changedClass : changed) {
            changedClasses.add(changedClass.getChangedClass());
        }
        final List<FakereplaceEntityManagerFactoryProxy> entityManagers = CurrentEntityManagerFactories.getEMFForEntities(changedClasses);


        final HibernateEnvironment hibEnv = CurrentEnvironment.getEnvironment().getService(HibernateEnvironment.class);
        final boolean replaceContainerManaged;
        if (hibEnv != null) {
            replaceContainerManaged = hibEnv.replaceContainerManagedEntityManagers();
        } else {
            replaceContainerManaged = true;
        }

        for(FakereplaceEntityManagerFactoryProxy entityManager :entityManagers) {
            if(!entityManager.isContainerManaged() ||
                    replaceContainerManaged) {
                entityManager.reload();
            }
        }
    }

}
