package a.org.fakereplace.integration.weld.clientproxy;

import javax.inject.Inject;

/**
 * @author Stuart Douglas
 */
public class DependentBean1 {

    @Inject
    private AppScopedBean1 appScopedBean;

    public String getValue() {
        return appScopedBean.getValue();
    }

    public void setValue(String value) {
        appScopedBean.setValue(value);
    }
}
