package org.swaggertools.core.config;

import lombok.Data;

@Data
public class Configuration {
    String name;
    String description;
    String defaultValue;
    boolean required;
    Class<Enum> enumClass;
}
