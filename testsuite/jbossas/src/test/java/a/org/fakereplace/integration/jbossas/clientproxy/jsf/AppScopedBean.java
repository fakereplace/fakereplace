package a.org.fakereplace.integration.jbossas.clientproxy.jsf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * @author Stuart Douglas
 */
@ApplicationScoped
@Named("app")
public class AppScopedBean {

    private String firstValue = "a";

    public String getFirstValue() {
        return firstValue;
    }
}
