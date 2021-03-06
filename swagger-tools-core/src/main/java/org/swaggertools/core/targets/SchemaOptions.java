package org.swaggertools.core.targets;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.config.ConfigurationProperty;

@Getter
@Setter
public class SchemaOptions {
    @ConfigurationProperty(description = "Models package name")
    private String modelPackage;
    @ConfigurationProperty(description = "String 'date-time' full class name")
    private String dateTimeClass;
}
