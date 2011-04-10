package org.fakereplace.integration.metawidget;

import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MetawidgetIntegrationInfo implements IntegrationInfo {

    public final static String BASE_PROPERTY_STYLE = "org.metawidget.inspector.impl.propertystyle.BasePropertyStyle";
    public final static String BASE_ACTION_STYLE = "org.metawidget.inspector.impl.actionstyle.BaseActionStyle";


    private static final Set<String> classNames;

    static {
        Set<String> ret = new HashSet<String>();
        ret.add(BASE_ACTION_STYLE);
        ret.add(BASE_PROPERTY_STYLE);
        classNames = Collections.unmodifiableSet(ret);
    }

    public String getClassChangeAwareName() {
        return "org.fakereplace.integration.metawidget.ClassRedefinitionPlugin";
    }

    public Set<String> getIntegrationTriggerClassNames() {
        return classNames;
    }

    public Set<String> getTrackedInstanceClassNames() {
        return classNames;
    }

    public ClassTransformer getTransformer() {
        return null;
    }

    public byte[] loadClass(String className) {
        return null;
    }

}
