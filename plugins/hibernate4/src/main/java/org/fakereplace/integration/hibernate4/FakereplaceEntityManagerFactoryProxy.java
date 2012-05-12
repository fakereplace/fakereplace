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

import java.util.Map;
import java.util.Set;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.ejb.HibernatePersistence;

/**
 * @author Stuart Douglas
 */
public class FakereplaceEntityManagerFactoryProxy implements EntityManagerFactory, HibernateEntityManagerFactory {

    private volatile EntityManagerFactory delegate;

    private final HibernatePersistence hibernatePersistence;
    private final String persistenceUnitName;
    private final Map properties;
    private final PersistenceUnitInfo persistenceUnitInfo;

    public FakereplaceEntityManagerFactoryProxy(final EntityManagerFactory delegate, final HibernatePersistence hibernatePersistence, final PersistenceUnitInfo persistenceUnitInfo, final Map properties) {
        this.delegate = delegate;
        this.hibernatePersistence = hibernatePersistence;
        this.properties = properties;
        this.persistenceUnitInfo = persistenceUnitInfo;
        this.persistenceUnitName = null;
        CurrentEntityManagerFactories.registerEntityManager(this);
    }

    public FakereplaceEntityManagerFactoryProxy(final EntityManagerFactory delegate, final HibernatePersistence hibernatePersistence, final String persistenceUnitName, final Map properties) {
        this.delegate = delegate;
        this.hibernatePersistence = hibernatePersistence;
        this.properties = properties;
        this.persistenceUnitName = persistenceUnitName;
        this.persistenceUnitInfo = null;
        CurrentEntityManagerFactories.registerEntityManager(this);
    }

    public void reload() {
        final EntityManagerFactory old = delegate;
        if (persistenceUnitInfo != null) {
            delegate = hibernatePersistence.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
        } else if (persistenceUnitName != null) {
            delegate = hibernatePersistence.createEntityManagerFactory(persistenceUnitName, properties);
        } else {
            delegate = hibernatePersistence.createEntityManagerFactory(properties);
        }
        //TODO: should we actually close this here?
        old.close();
    }

    @Override
    public EntityManager createEntityManager() {
        return delegate.createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(final Map map) {
        return delegate.createEntityManager(map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return ((HibernateEntityManagerFactory)delegate).getSessionFactory();
    }

    public boolean isContainerManaged() {
        return persistenceUnitInfo != null;
    }

    public boolean containsEntity(final Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                delegate.getMetamodel().entity(clazz);
                return true;
            } catch (IllegalArgumentException e) {

            }
        }
        return false;
    }
}
