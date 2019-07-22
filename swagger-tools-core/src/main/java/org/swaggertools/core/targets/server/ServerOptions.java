package org.swaggertools.core.targets.server;

import lombok.Data;
import org.swaggertools.core.config.ConfigurationProperty;

@Data
public class ServerOptions {
    @ConfigurationProperty(description = "Server classes target directory", required = true)
    String location;
    @ConfigurationProperty(description = "Server classes package name", required = true)
    String apiPackage;
    @ConfigurationProperty(description = "Models package name", required = true)
    String modelPackage;
    @ConfigurationProperty(description = "Server classes name suffix", defaultValue = "Api")
    String apiSuffix = "Api";
    @ConfigurationProperty(description = "Generate reactive, non-blocking API", defaultValue = "false")
    boolean reactive = false;
    @ConfigurationProperty(description = "Implementation dialect [Spring/JaxRS]", defaultValue = "Spring")
    ServerDialect dialect = ServerDialect.Spring;
    @ConfigurationProperty(description = "Annotate method parameters with javax.validation.constraints.Valid", defaultValue = "false")
    boolean validation = false;
}