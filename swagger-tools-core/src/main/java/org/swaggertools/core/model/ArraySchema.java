package org.swaggertools.core.model;

import lombok.Data;

@Data
public class ArraySchema extends Schema {
    Schema itemsSchema;
    Integer maxLength;
    Integer minLength;
    Boolean uniqueItems;

    @Override
    public boolean isCollection() {
        return true;
    }
}
