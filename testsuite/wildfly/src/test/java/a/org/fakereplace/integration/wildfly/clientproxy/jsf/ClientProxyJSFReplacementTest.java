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

package a.org.fakereplace.integration.wildfly.clientproxy.jsf;

import java.io.IOException;
import java.net.URL;
import javax.naming.NamingException;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import a.org.fakereplace.testsuite.shared.HttpUtils;
import a.org.fakereplace.testsuite.shared.RemoteClassReplacer;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientProxyJSFReplacementTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(AppScopedBean.class)
                .addAsWebInfResource(ClientProxyJSFReplacementTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(ClientProxyJSFReplacementTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebResource(ClientProxyJSFReplacementTest.class.getPackage(), "index.xhtml", "index.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
