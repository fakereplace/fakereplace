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

package a.org.fakereplace.integration.wildfly.resteasy.addresource;

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

import java.net.URL;

/**
 * Tests changing a JAX-RS method from a String to a JAXB object
 *
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class AddJAXRSResourceTestCase {

    public static final String DEPLOYMENT_NAME = "jaxrschangejaxb.war";

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME);
        war.addClasses(AddJAXRSResourceTestCase.class, HelloWorldResource.class, HelloWorldPathApplication.class);
        return war;
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testChangingJaxbModel() throws Exception {

        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet(url + "hellopath/helloworld"));
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        String result = HttpUtils.getContent(response);
        Assert.assertEquals("root", result);
        RemoteClassReplacer r = new RemoteClassReplacer();
        r.addNewClass(AddedResource.class);
        r.replaceQueuedClasses(DEPLOYMENT_NAME);

        response = client.execute(new HttpGet(url + "hellopath/added"));
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        result = HttpUtils.getContent(response);
        Assert.assertEquals("added", result);
    }
}
