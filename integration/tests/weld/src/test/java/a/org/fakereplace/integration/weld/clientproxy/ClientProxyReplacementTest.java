package a.org.fakereplace.integration.weld.clientproxy;

import javax.inject.Inject;

import a.org.fakereplace.integration.weld.util.ClassReplacer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Stuart Douglas
 */
public class ClientProxyReplacementTest extends Arquillian {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar" )
                .addPackage(ClientProxyReplacementTest.class.getPackage());
    }

    @Inject
    private DependentBean dependentBean;

    @Test
    public void testClientProxyReplacement() throws InterruptedException {
        dependentBean.setValue("Hello CDI");
        final ClassReplacer replacer = new ClassReplacer();
        replacer.queueClassForReplacement(AppScopedBean.class, AppScopedBean1.class);
        replacer.queueClassForReplacement(DependentBean.class, DependentBean1.class);
        replacer.replaceQueuedClasses();
        Assert.assertEquals(dependentBean.getValue(), "Hello CDI");

    }
}
