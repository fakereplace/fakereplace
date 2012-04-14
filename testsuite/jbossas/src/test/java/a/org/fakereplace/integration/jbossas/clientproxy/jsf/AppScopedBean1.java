package a.org.fakereplace.integration.jbossas.clientproxy.jsf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * @author Stuart Douglas
 */
@ApplicationScoped
@Named("app")
public class AppScopedBean1 {

    private String firstValue = "a";

    public String getFirstValue() {
        return firstValue;
    }

    public String getSecondValue() {
        return "b";
    }
}
