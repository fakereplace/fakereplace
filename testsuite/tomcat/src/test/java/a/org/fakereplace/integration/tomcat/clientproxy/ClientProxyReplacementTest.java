package a.org.fakereplace.integration.tomcat.clientproxy;

import a.org.fakereplace.testsuite.shared.HttpUtils;
import a.org.fakereplace.testsuite.shared.RemoteClassReplacer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

@RunWith(Arquillian.class)
@RunAsClient
public class ClientProxyReplacementTest {
    @Deployment
    public static WebArchive createDeployment() {
        Package classPackage = ClientProxyReplacementTest.class.getPackage();

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(UsedClass.class)
                .addAsWebInfResource(classPackage, "web.xml", "web.xml")
                .addAsWebResource(classPackage, "index.jsp", "index.jsp");
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testClassChanges() throws InterruptedException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse result = client.execute(new HttpGet(url.toExternalForm() + "index.jsp"));
        String content = HttpUtils.getContent(result);
        Assert.assertTrue(content, content.contains("index.jsp: UsedClass"));
        Assert.assertFalse(content, content.contains("index1.jsp: UsedClass1"));

        final RemoteClassReplacer replacer = new RemoteClassReplacer();
        replacer.queueClassForReplacement(UsedClass.class, UsedClass1.class);
        replacer.queueResourceForReplacement(getClass(), "index.jsp", "index1.jsp");
        replacer.replaceQueuedClasses("test");

        result = client.execute(new HttpGet(url.toExternalForm() + "index.jsp"));
        content = HttpUtils.getContent(result);

        Assert.assertTrue(content, content.contains("index1.jsp: UsedClass1"));
        Assert.assertFalse(content, content.contains("index.jsp: UsedClass"));

        System.out.println(url.toExternalForm());
    }
}
