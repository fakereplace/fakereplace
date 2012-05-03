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

package a.org.fakereplace.integration.jbossas.clientproxy.remoteejb;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import a.org.fakereplace.integration.jbossas.util.ClassReplacer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientProxyReplacementTest   {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage(ClientProxyReplacementTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private InitialContext initialContext;

    @Test
    public void testClientProxyReplacement() throws InterruptedException, NamingException {
        final RemoteInterface remote = (RemoteInterface) initialContext.lookup("ejb:/test/RemoteEjb!" + RemoteInterface.class.getName());

        remote.setValue("Hello CDI");
        final ClassReplacer replacer = new ClassReplacer();
        replacer.queueClassForReplacement(AppScopedBean.class, AppScopedBean1.class);
        replacer.queueClassForReplacement(RemoteEjb.class, RemoteEjb1.class);
        replacer.replaceQueuedClasses("test.jar");
        Assert.assertEquals("Hello CDI", remote.getValue());

    }
}
