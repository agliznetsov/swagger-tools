package org.swaggertools.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
public class ObjectSchema extends Schema {
    Schema additionalProperties;
    String superSchema;
    String discriminator;
    Collection<Property> properties;

    public ObjectSchema(String name) {
        setName(name);
    }

    @Override
    public boolean isCollection() {
        return additionalProperties != null;
    }
}
