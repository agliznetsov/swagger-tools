package org.swaggertools.core.config;

import java.util.List;
import java.util.Map;

public abstract class AutoConfigurable<T> implements Configurable {
    protected final T options;

    public T getOptions() {
        return options;
    }

    protected AutoConfigurable(T options) {
        this.options = options;
    }

    @Override
    public List<Configuration> getConfigurations() {
        //TODO: read fields
        return null;
    }

    @Override
    public void configure(Map<String, String> configValues) {
        //TODO: set field
    }

    protected void validateConfiguration() {
        //TODO: check required
    }
}
