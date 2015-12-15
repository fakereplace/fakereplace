package org.fakereplace.integration.wildfly.hibernate5;

import javax.persistence.EntityManagerFactory;

/**
 * @author Stuart Douglas
 */
public interface HackPersistenceUnitService {

    EntityManagerFactory emf();

}
