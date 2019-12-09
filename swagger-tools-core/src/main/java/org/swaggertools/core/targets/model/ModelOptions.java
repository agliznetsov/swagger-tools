package org.swaggertools.core.targets.model;

import lombok.Data;
import org.swaggertools.core.config.ConfigurationProperty;
import org.swaggertools.core.targets.SchemaOptions;

@Data
public class ModelOptions extends SchemaOptions {
    @ConfigurationProperty(description = "Model classes target directory", required = true)
    String location;
    @ConfigurationProperty(description = "Initialize collection fields with empty collection", defaultValue = "true")
    boolean initializeCollections = true;
    @ConfigurationProperty(description = "Annotate model classes with lombok to generate equals/hashCode/toString", defaultValue = "false")
    boolean lombok = false;
    @ConfigurationProperty(description = "Use unique methodName in @Builder annotation", defaultValue = "false")
    boolean lombokUniqueBuilder = false;
    @ConfigurationProperty(description = "Use @SuperBuilder annotation", defaultValue = "false")
    boolean lombokSuperBuilder = false;
    @ConfigurationProperty(description = "Annotate model properties with javax.validation.constraints.*", defaultValue = "false")
    boolean validation = false;
}
