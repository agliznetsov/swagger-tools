package org.swaggertools.core.model;

import lombok.Data;

import java.util.Collection;

@Data
public abstract class Schema {
    String name;

    public String getDefaultValue() {
        return null;
    }

    public boolean isReadOnly() {
        return false;
    }

    public Collection<String> getEnumValues() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }
}
