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

package a.org.fakereplace.integration.wildfly.clientproxy.remoteejb;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import a.org.fakereplace.testsuite.shared.RemoteClassReplacer;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientProxyReplacementTest   {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(AppScopedBean.class, RemoteEjb.class, RemoteInterface.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testClientProxyReplacement() throws InterruptedException, NamingException {
        Properties prop = new Properties();
        prop.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        InitialContext initialContext = new InitialContext(prop);
        final RemoteInterface remote = (RemoteInterface) initialContext.lookup("ejb:/test/RemoteEjb!" + RemoteInterface.class.getName());

        remote.setValue("Hello CDI");
        final RemoteClassReplacer replacer = new RemoteClassReplacer();
        replacer.queueClassForReplacement(AppScopedBean.class, AppScopedBean1.class);
        replacer.queueClassForReplacement(RemoteEjb.class, RemoteEjb1.class);
        replacer.replaceQueuedClasses("test.war");
        Assert.assertEquals("Hello CDI", remote.getValue());

    }
}
