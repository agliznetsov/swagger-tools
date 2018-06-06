package org.swaggertools.core.model;

import lombok.Data;

@Data
public class Parameter {
    String name;
    Schema schema;
    ParameterKind kind;
    boolean required;
}
