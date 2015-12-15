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

package a.org.fakereplace.integration.wildfly.hibernate5.basic.addcolumn;

import a.org.fakereplace.testsuite.shared.RemoteClassReplacer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class Hibernate5AddColumnTestCase {

    public static final String DEPLOYMENT_NAME = "Hibernate5AddColumnTestCase.war";

    @Deployment
    public static Archive deploy() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME)
                .addClasses(Employee.class, EmployeeEjb.class, RemoteEmployee.class)
                .addAsResource(Hibernate5AddColumnTestCase.class.getPackage(),  "persistence.xml", "META-INF/persistence.xml");
    }

    @Test
    public void testAddingColumn() throws NamingException {

        Properties prop = new Properties();
        prop.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        InitialContext initialContext = new InitialContext(prop);
        RemoteEmployee ejb = (RemoteEmployee)initialContext.lookup("ejb:/Hibernate5AddColumnTestCase/" + EmployeeEjb.class.getSimpleName() + "!" + RemoteEmployee.class.getName());
        ejb.saveEntity(1);
        Assert.assertEquals("1-name", ejb.getEntityDesc(1));
        final RemoteClassReplacer replacer = new RemoteClassReplacer();
        replacer.queueClassForReplacement(Employee.class, Employee1.class);
        replacer.queueClassForReplacement(EmployeeEjb.class, EmployeeEjb1.class);
        replacer.replaceQueuedClasses(DEPLOYMENT_NAME);
        ejb.saveEntity(2);
        Assert.assertEquals("2-name-address", ejb.getEntityDesc(2));
    }

}
