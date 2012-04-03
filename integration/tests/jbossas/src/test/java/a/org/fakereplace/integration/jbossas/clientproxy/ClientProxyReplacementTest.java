package a.org.fakereplace.integration.jbossas.clientproxy;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
public class ClientProxyReplacementTest   {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage(ClientProxyReplacementTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private DependentBean dependentBean;

    @Test
    public void testClientProxyReplacement() throws InterruptedException {
        //dependentBean.setValue("Hello CDI");
        //Assert.assertEquals("Hello CDI", dependentBean.getValue());

    }
}
