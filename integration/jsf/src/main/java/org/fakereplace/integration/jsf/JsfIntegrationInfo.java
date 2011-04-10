package org.fakereplace.integration.jsf;

import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;

import java.util.Collections;
import java.util.Set;

public class JsfIntegrationInfo implements IntegrationInfo {

    public String getClassChangeAwareName() {
        return "org.fakereplace.integration.jsf.ClassRedefinitionPlugin";
    }

    public Set<String> getIntegrationTriggerClassNames() {
        return Collections.singleton("javax.faces.webapp.FacesServlet");
    }

    public Set<String> getTrackedInstanceClassNames() {
        return Collections.singleton("javax.el.BeanELResolver");
    }

    public ClassTransformer getTransformer() {
        return null;
    }

    public byte[] loadClass(String className) {
        return null;
    }

}
