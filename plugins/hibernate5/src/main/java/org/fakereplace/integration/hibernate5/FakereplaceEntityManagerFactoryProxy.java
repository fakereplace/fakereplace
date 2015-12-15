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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;

/**
 * @author Stuart Douglas
 */
public class FakereplaceEntityManagerFactoryProxy implements HibernateEntityManagerFactory {

    private volatile HibernateEntityManagerFactory delegate;
    private final EntityManagerFactoryBuilder builder;

    public FakereplaceEntityManagerFactoryProxy(final HibernateEntityManagerFactory delegate, EntityManagerFactoryBuilder builder) {
        this.delegate = delegate;
        this.builder = builder;
        CurrentEntityManagerFactories.registerEntityManager(this);
    }

    public void reload() {
        FakereplaceEntityManagerFactoryProxy build = (FakereplaceEntityManagerFactoryProxy) builder.build();
        CurrentEntityManagerFactories.removeEmf(build);
        delegate = build.getDelegate();
    }

    public HibernateEntityManagerFactory getDelegate() {
        return delegate;
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
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return delegate.createEntityManager(synchronizationType);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return delegate.createEntityManager(synchronizationType, map);
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
    public void addNamedQuery(String name, Query query) {
        delegate.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return delegate.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        delegate.addNamedEntityGraph(graphName, entityGraph);
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

    @Override
    public SessionFactoryImplementor getSessionFactory() {
        return delegate.getSessionFactory();
    }

    @Override
    public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
        return delegate.findEntityGraphsByType(entityClass);
    }

    @Override
    public String getEntityManagerFactoryName() {
        return delegate.getEntityManagerFactoryName();
    }

    @Override
    public EntityType getEntityTypeByName(String entityName) {
        return delegate.getEntityTypeByName(entityName);
    }
}
