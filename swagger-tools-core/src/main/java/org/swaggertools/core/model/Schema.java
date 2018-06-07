package org.swaggertools.core.model;

import lombok.Data;

import java.util.Collection;

@Data
public class Schema {
    String name;
    String ref;
    String type;
    String format;
    Schema items;
    Schema additionalProperties;
    String superSchema;
    String discriminator;
    String defaultValue;
    Collection<String> enumValues;
    Collection<Property> properties;

    public boolean isCollection() {
        return "array".equals(type) || "map".equals(type);
    }
}
