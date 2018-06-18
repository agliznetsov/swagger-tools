package org.swaggertools.core.config;

import java.util.List;
import java.util.Map;

public interface Configurable {
    String getGroupName();
    List<Configuration> getConfigurations();
    void configure(Map<String, String> configValues);
}
