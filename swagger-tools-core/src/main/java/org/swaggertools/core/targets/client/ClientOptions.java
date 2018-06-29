package org.swaggertools.core.targets.client;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.config.ConfigurationProperty;

@Getter
@Setter
public class ClientOptions {
    @ConfigurationProperty(description = "Server classes target directory", required = true)
    String location;

    @ConfigurationProperty(description = "Client classes package name", required = true)
    String clientPackage;

    @ConfigurationProperty(description = "Models package name", required = true)
    String modelPackage;

    @ConfigurationProperty(description = "Client classes name suffix", defaultValue = "Client")
    String clientSuffix = "Client";

    @ConfigurationProperty(description = "Client implementation dialect", defaultValue = "RestTemplate")
    ClientDialect dialect = ClientDialect.RestTemplate;

    @ConfigurationProperty(description = "Client factory class name. If empty no factory is generated.", defaultValue = "")
    String factoryName = "";
}
