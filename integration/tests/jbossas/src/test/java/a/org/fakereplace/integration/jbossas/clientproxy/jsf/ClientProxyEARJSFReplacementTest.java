package a.org.fakereplace.integration.jbossas.clientproxy.jsf;

import java.io.IOException;
import java.net.URL;

import javax.naming.NamingException;

import a.org.fakereplace.integration.jbossas.util.ClassReplacer;
import a.org.fakereplace.integration.jbossas.util.HttpUtils;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
@Ignore("The protocol does not support EAR's yet")
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
        final ClassReplacer replacer = new ClassReplacer();
        replacer.queueClassForReplacement(AppScopedBean.class, AppScopedBean1.class);
        replacer.queueResourceForReplacement(getClass(), "index.xhtml", "index1.xhtml");
        replacer.replaceQueuedClasses("test.war");
        result = client.execute(new HttpGet(url.toExternalForm() + "index.jsf"));
        content = HttpUtils.getContent(result);

        Assert.assertTrue(content, content.contains("FIRST: a"));
        Assert.assertTrue(content, content.contains("SECOND: b"));
    }
}
