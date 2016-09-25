package org.fakereplace.client.standalone.embedded;

import org.fakereplace.api.Extension;
import org.fakereplace.transformation.FakereplaceTransformer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FakereplaceMonitoringExtension implements Extension {
    public FakereplaceMonitoringExtension() {
        new FakereplaceMonitoring().startMonitoring();
    }

    @Override
    public List<FakereplaceTransformer> getTransformers() {
        return Collections.emptyList();
    }

    @Override
    public String getClassChangeAwareName() {
        return null;
    }

    @Override
    public Set<String> getIntegrationTriggerClassNames() {
        return Collections.emptySet();
    }

    @Override
    public String getEnvironment() {
        return null;
    }

    @Override
    public Set<String> getTrackedInstanceClassNames() {
        return Collections.emptySet();
    }
}
