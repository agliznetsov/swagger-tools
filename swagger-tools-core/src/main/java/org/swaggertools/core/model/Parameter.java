package org.swaggertools.core.model;

import static org.swaggertools.core.util.NameUtils.camelCase;
import static org.swaggertools.core.util.NameUtils.javaIdentifier;

import lombok.Data;

@Data
public class Parameter {
    String name;
    Schema schema;
    ParameterKind kind;
    boolean required;

    public String getJavaIdentifier() {
        return camelCase(javaIdentifier(name));
    }
}
