package org.swaggertools.core.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Collection;

@Data
public class PrimitiveSchema extends Schema {
    PrimitiveType type;
    String format;
    boolean readOnly;
    String defaultValue;
    Collection<String> enumValues;
    BigDecimal maximum;
    BigDecimal minimum;
    Integer maxLength;
    Integer minLength;
    String pattern;
}
