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

package a.org.fakereplace.integration.wildfly.hibernate5.basic.addentity;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import a.org.fakereplace.testsuite.shared.RemoteClassReplacer;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
@Ignore
public class Hibernate5AddEntityTestCase {

    public static final String DEPLOYMENT_NAME = "Hibernate5AddEntityTestCase.war";

    @Deployment
    public static Archive deploy() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME)
                .addClasses(Employee.class, EmployeeEjb.class, RemoteEmployee.class)
                .addAsResource(Hibernate5AddEntityTestCase.class.getPackage(), "persistence.xml", "META-INF/persistence.xml");
        System.out.println(webArchive);
        return webArchive;
    }

    @Test
    public void testAddingEntity() throws NamingException {
        Properties prop = new Properties();
        prop.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        InitialContext initialContext = new InitialContext(prop);
        RemoteEmployee ejb = (RemoteEmployee)initialContext.lookup("ejb:/Hibernate5AddEntityTestCase/" + EmployeeEjb.class.getSimpleName() + "!" + RemoteEmployee.class.getName());
        ejb.saveEntity(1);
        Assert.assertEquals("1-name", ejb.getEntityDesc(1));
        final RemoteClassReplacer replacer = new RemoteClassReplacer();
        replacer.queueClassForReplacement(Employee.class, Employee1.class);
        replacer.queueClassForReplacement(EmployeeEjb.class, EmployeeEjb1.class);
        replacer.addNewClass(AddedEntity.class);
        replacer.replaceQueuedClasses(DEPLOYMENT_NAME);
        ejb.saveEntity(2);
        Assert.assertEquals("2-name-address", ejb.getEntityDesc(2));
    }

}
