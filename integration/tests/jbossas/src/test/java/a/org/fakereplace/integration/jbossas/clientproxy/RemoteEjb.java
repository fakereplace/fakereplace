package a.org.fakereplace.integration.jbossas.clientproxy;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Stuart Douglas
 */
@Stateless
public class RemoteEjb implements RemoteInterface {

    @Inject
    private AppScopedBean appScopedBean;

    public String getValue() {
        return "FAIL";
    }

    public void setValue(String value) {
        appScopedBean.setValue(value);
    }
}
