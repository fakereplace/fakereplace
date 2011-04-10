package org.fakereplace.integration.seam;

import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SeamIntegrationInfo implements IntegrationInfo {

    public String getClassChangeAwareName() {
        return "org.fakereplace.integration.seam.ClassRedefinitionPlugin";
    }

    public Set<String> getIntegrationTriggerClassNames() {
        return Collections.singleton("org.jboss.seam.servlet.SeamFilter");
    }

    public Set<String> getTrackedInstanceClassNames() {
        Set<String> ret = new HashSet<String>();
        ret.add("org.jboss.seam.servlet.SeamFilter");
        return ret;
    }

    public ClassTransformer getTransformer() {
        return new SeamTransformer();
    }

    public byte[] loadClass(String className) {
        return null;
    }

}
