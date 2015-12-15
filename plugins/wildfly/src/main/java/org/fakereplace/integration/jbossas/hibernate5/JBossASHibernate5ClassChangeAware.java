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

package org.fakereplace.integration.jbossas.hibernate5;

import org.fakereplace.api.AttachmentKeys;
import org.fakereplace.api.Attachments;
import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.data.InstanceTracker;
import org.jboss.as.jpa.service.PersistenceUnitServiceImpl;
import org.jboss.as.naming.WritableServiceBasedNamingStore;
import org.jboss.as.server.CurrentServiceContainer;
import org.jipijapa.plugin.spi.PersistenceProviderAdaptor;
import org.jipijapa.plugin.spi.PersistenceUnitMetadata;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class JBossASHibernate5ClassChangeAware implements ClassChangeAware {

    @Override
    public void beforeChange(final List<Class<?>> changed, final List<ClassIdentifier> added, Attachments attachments) {

    }

    @Override
    public void afterChange(final List<ChangedClass> changed, final List<ClassIdentifier> added, Attachments attachments) {
        final Set<Class<?>> changedClasses = new HashSet<Class<?>>();
        boolean replace = false;
        for (ChangedClass changedClass : changed) {
            if (changedClass.getChangedClass().isAnnotationPresent(Entity.class) ||
                    !changedClass.getChangedAnnotationsByType(Entity.class).isEmpty()) {
                replace = true;
            }
            changedClasses.add(changedClass.getChangedClass());
        }
        if (!replace) {
            return;
        }

        final String deploymentName = attachments.get(AttachmentKeys.DEPLOYMENT_NAME);

        final Set<PersistenceUnitServiceImpl> puServices = (Set<PersistenceUnitServiceImpl>) InstanceTracker.get(JBossASHibernate5Extension.PERSISTENCE_UNIT_SERVICE);

        //AS7 caches annotations so it does not have to hang onto the Jandex index
        //we need to update this index
        try {
            //final Module hib4Module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.jboss.as.jpa"));
            //final Class<?> annotationScanner = hib4Module.getClassLoader().loadClass("org.jboss.as.jpa.hibernate5.HibernateAnnotationScanner");
            //final Field classesInJar = annotationScanner.getDeclaredField("CLASSES_IN_JAR_CACHE");
            //classesInJar.setAccessible(true);
            //final Map<PersistenceUnitMetadata, Map<URL, Map<Class<? extends Annotation>, Set<Class<?>>>>> cache = (Map<PersistenceUnitMetadata, Map<URL, Map<Class<? extends Annotation>, Set<Class<?>>>>>) classesInJar.get(null);

            final Field puField = PersistenceUnitServiceImpl.class.getDeclaredField("pu");
            puField.setAccessible(true);
            final Field persistenceProviderAdaptorField = PersistenceUnitServiceImpl.class.getDeclaredField("persistenceProviderAdaptor");
            persistenceProviderAdaptorField.setAccessible(true);


            WritableServiceBasedNamingStore.pushOwner(CurrentServiceContainer.getServiceContainer());
            try {
                for (PersistenceUnitServiceImpl puService : puServices) {

                    final Object proxy = puService.getEntityManagerFactory();
                    final PersistenceProviderAdaptor adaptor = (PersistenceProviderAdaptor) persistenceProviderAdaptorField.get(puService);
                    if (adaptor != null && proxy != null) {
                        final PersistenceUnitMetadata pu = (PersistenceUnitMetadata) puField.get(puService);

                        //final Map<URL, Map<Class<? extends Annotation>, Set<Class<?>>>> urlAnnotations = cache.get(pu);

                        //TODO: handle new entities

                        adaptor.beforeCreateContainerEntityManagerFactory(pu);
                        try {
                            Method method = proxy.getClass().getDeclaredMethod("reload");
                            method.invoke(proxy);
                        } finally {
                            adaptor.afterCreateContainerEntityManagerFactory(pu);
                        }
                    }

                }
            } finally {
                WritableServiceBasedNamingStore.popOwner();
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
