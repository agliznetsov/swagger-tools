package org.swaggertools.core.model;

import lombok.Data;

import java.util.Collection;

@Data
public class PrimitiveSchema extends Schema {
    PrimitiveType type;
    String format;
    String defaultValue;
    Collection<String> enumValues;
}
