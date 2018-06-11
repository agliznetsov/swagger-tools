package org.swaggertools.core.model;

import lombok.Data;

@Data
public class ArraySchema extends Schema {
    Schema itemsSchema;

    @Override
    public boolean isCollection() {
        return true;
    }
}
