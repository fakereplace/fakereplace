package a.org.fakereplace.integration.jbossas.clientproxy.remoteejb;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Stuart Douglas
 */
@Stateless
public class RemoteEjb1 implements RemoteInterface {

    @Inject
    private AppScopedBean1 appScopedBean;

    public String getValue() {
        return appScopedBean.getValue();
    }

    public void setValue(String value) {
        appScopedBean.setValue(value);
    }
}
