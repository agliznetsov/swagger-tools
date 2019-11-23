package org.swaggertools.core.targets;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.config.ConfigurationProperty;

@Getter
@Setter
public class SchemaOptions {
    @ConfigurationProperty(description = "Models package name", required = true)
    private String modelPackage;
    @ConfigurationProperty(description = "String 'date-time' full class name", required = false)
    private String dateTimeClass;
}
