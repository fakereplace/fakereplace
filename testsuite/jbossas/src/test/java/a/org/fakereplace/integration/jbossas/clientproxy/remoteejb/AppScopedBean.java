package a.org.fakereplace.integration.jbossas.clientproxy.remoteejb;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Stuart Douglas
 */
@ApplicationScoped
public class AppScopedBean {

    private String value = "a";

    public void setValue(final String value) {
        this.value = value;
    }
}
