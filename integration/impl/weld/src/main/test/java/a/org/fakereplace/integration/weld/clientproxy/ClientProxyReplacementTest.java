package a.org.fakereplace.integration.weld.clientproxy;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

/**
 * @author Stuart Douglas
 */
public class ClientProxyReplacementTest extends Arquillian {

    @Deployment
    public Archive<?> deploy() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar" )
                .addClasses(ClientProxyReplacementTest.class, AppScopedBean.class);
    }

    @Test
    public void testClientProxyReplacement() {

    }
}
