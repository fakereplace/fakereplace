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

package org.fakereplace.integration.wildfly.hibernate5;

import org.fakereplace.api.AttachmentKeys;
import org.fakereplace.api.Attachments;
import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.data.InstanceTracker;
import org.jboss.as.jpa.service.PersistenceUnitServiceImpl;
import org.jboss.as.jpa.service.PhaseOnePersistenceUnitServiceImpl;
import org.jboss.as.naming.WritableServiceBasedNamingStore;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jipijapa.plugin.spi.PersistenceProviderAdaptor;
import org.jipijapa.plugin.spi.PersistenceUnitMetadata;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Stuart Douglas
 */
public class WildflyHibernate5ClassChangeAware implements ClassChangeAware {

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

        final Set<PersistenceUnitServiceImpl> puServices = (Set<PersistenceUnitServiceImpl>) InstanceTracker.get(WildflyHibernate5Extension.PERSISTENCE_UNIT_SERVICE);
        final Set<PhaseOnePersistenceUnitServiceImpl> phaseOneServices = (Set<PhaseOnePersistenceUnitServiceImpl>) InstanceTracker.get(WildflyHibernate5Extension.PERSISTENCE_PHASE_ONE_SERVICE);

        //AS7 caches annotations so it does not have to hang onto the Jandex index
        //we need to update this index
        try {


            WritableServiceBasedNamingStore.pushOwner(CurrentServiceContainer.getServiceContainer());
            try {
                for (PersistenceUnitServiceImpl puService : puServices) {
                    try {
                        //make sure the service is started before stopping
                        if(puService.getExecutorInjector().getOptionalValue() != null) {
                            doServiceStop(puService);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (PhaseOnePersistenceUnitServiceImpl puService : phaseOneServices) {
                    try {
                        if (puService.getExecutorInjector().getOptionalValue() != null) {
                            doServiceStop(puService);
                            doServiceStart(puService);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (PersistenceUnitServiceImpl puService : puServices) {
                    try {
                        //make sure the service is started before stopping
                        if(puService.getExecutorInjector().getOptionalValue() != null) {
                            doServiceStart(puService);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                WritableServiceBasedNamingStore.popOwner();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doServiceStop(Service puService) throws InterruptedException {
        final CountDownLatch stopLatch = new CountDownLatch(1);
        final AtomicBoolean async = new AtomicBoolean(false);
        puService.stop(new StopContext() {
            @Override
            public void asynchronous() throws IllegalStateException {
                async.set(true);
            }

            @Override
            public void complete() throws IllegalStateException {
                stopLatch.countDown();
            }

            @Override
            public long getElapsedTime() {
                return 0;
            }

            @Override
            public ServiceController<?> getController() {
                return null;
            }

            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });
        if(async.get()) {
            stopLatch.await();
        }
    }

    private void doServiceStart(Service puService) throws StartException, InterruptedException {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean async = new AtomicBoolean(false);
        async.set(false);
        puService.start(new StartContext() {
            @Override
            public void asynchronous() {
                async.set(true);
            }

            @Override
            public void failed(StartException reason) throws IllegalStateException {
                reason.printStackTrace();
                startLatch.countDown();
            }

            @Override
            public void complete() throws IllegalStateException {
                startLatch.countDown();
            }

            @Override
            public ServiceTarget getChildTarget() {
                return null;
            }

            @Override
            public long getElapsedTime() {
                return 0;
            }

            @Override
            public ServiceController<?> getController() {
                return null;
            }

            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });

        if(async.get()) {
            startLatch.await();
        }
    }
}
