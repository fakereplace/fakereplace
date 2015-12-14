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

package a.org.fakereplace.integration.wildfly.resteasy.changejaxbmodel;

import java.net.URL;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests changing a JAX-RS method from a String to a JAXB object
 *
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ChangeJaxbModelTestCase {

    public static final String DEPLOYMENT_NAME = "jaxrschangejaxb.war";

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME);
        war.addClasses(ChangeJaxbModelTestCase.class, HelloWorldResource.class, HelloWorldPathApplication.class, JaxbModel.class);
        return war;
    }

    @ArquillianResource
    private URL url;

    private String performCall(String urlPattern) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse result = client.execute(new HttpGet(url + urlPattern));
        return HttpUtils.getContent(result);
    }

    @Test
    public void testChangingJaxbModel() throws Exception {
        String result = performCall("hellopath/helloworld");
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jaxbModel><first>John</first></jaxbModel>", result);
        RemoteClassReplacer r = new RemoteClassReplacer();
        r.queueClassForReplacement(HelloWorldResource.class, HelloWorldResource1.class);
        r.queueClassForReplacement(JaxbModel.class, JaxbModel1.class);
        r.replaceQueuedClasses(DEPLOYMENT_NAME);
        result = performCall("hellopath/helloworld");
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jaxbModel><first>John</first><last>Citizen</last></jaxbModel>", result);
    }
}
