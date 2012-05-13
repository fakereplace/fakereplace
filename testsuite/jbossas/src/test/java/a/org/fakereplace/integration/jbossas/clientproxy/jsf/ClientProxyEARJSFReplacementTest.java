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

package a.org.fakereplace.integration.jbossas.clientproxy.jsf;

import java.io.IOException;
import java.net.URL;

import javax.naming.NamingException;

import a.org.fakereplace.testsuite.shared.HttpUtils;
import a.org.fakereplace.testsuite.shared.RemoteClassReplacer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientProxyEARJSFReplacementTest {

    @Deployment
    public static Archive<?> deploy() {
        final WebArchive war =  ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(AppScopedBean.class)
                .addAsWebInfResource(ClientProxyEARJSFReplacementTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(ClientProxyEARJSFReplacementTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebResource(ClientProxyEARJSFReplacementTest.class.getPackage(), "index.xhtml", "index.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(war);
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testClientProxyReplacement() throws InterruptedException, NamingException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse result = client.execute(new HttpGet(url.toExternalForm() + "index.jsf"));
        String content = HttpUtils.getContent(result);
        Assert.assertTrue(content, content.contains("FIRST: a"));
        Assert.assertFalse(content, content.contains("SECOND: b"));
        final RemoteClassReplacer replacer = new RemoteClassReplacer();
        replacer.queueClassForReplacement(AppScopedBean.class, AppScopedBean1.class);
        replacer.queueResourceForReplacement(getClass(), "index.xhtml", "index1.xhtml");
        replacer.replaceQueuedClasses("test.war");
        result = client.execute(new HttpGet(url.toExternalForm() + "index.jsf"));
        content = HttpUtils.getContent(result);

        Assert.assertTrue(content, content.contains("FIRST: a"));
        Assert.assertTrue(content, content.contains("SECOND: b"));
    }
}
