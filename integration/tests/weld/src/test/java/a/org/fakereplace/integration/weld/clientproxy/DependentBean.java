package a.org.fakereplace.integration.weld.clientproxy;

import javax.inject.Inject;

/**
 * @author Stuart Douglas
 */
public class DependentBean {

    @Inject
    private AppScopedBean appScopedBean;

    public String getValue() {
        return "FAIL";
    }

    public void setValue(String value) {
        appScopedBean.setValue(value);
    }
}
