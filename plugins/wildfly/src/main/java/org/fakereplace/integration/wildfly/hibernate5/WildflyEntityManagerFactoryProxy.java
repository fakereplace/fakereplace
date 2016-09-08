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

package org.fakereplace.integration.wildfly.hibernate5;

import org.jipijapa.plugin.spi.PersistenceUnitService;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * @author Stuart Douglas
 */
public class WildflyEntityManagerFactoryProxy implements EntityManagerFactory {

    private final PersistenceUnitService persistenceUnitService;

    public WildflyEntityManagerFactoryProxy(PersistenceUnitService persistenceUnitService) {
        this.persistenceUnitService = persistenceUnitService;
    }

    @Override
    public EntityManager createEntityManager() {
        return unwrap().createEntityManager();
    }

    private EntityManagerFactory unwrap() {
        return ((HackPersistenceUnitService)persistenceUnitService).emf();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return unwrap().createEntityManager(map);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return unwrap().createEntityManager(synchronizationType);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return unwrap().createEntityManager(synchronizationType, map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return unwrap().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return unwrap().getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return unwrap().isOpen();
    }

    @Override
    public void close() {
        unwrap().close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return unwrap().getProperties();
    }

    @Override
    public Cache getCache() {
        return unwrap().getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return unwrap().getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        unwrap().addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return unwrap().unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        unwrap().addNamedEntityGraph(graphName, entityGraph);
    }
}
