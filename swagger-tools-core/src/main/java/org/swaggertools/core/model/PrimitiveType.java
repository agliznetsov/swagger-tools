package org.swaggertools.core.model;

public enum PrimitiveType {
    INTEGER, NUMBER, STRING, BOOLEAN, FILE;

    public static PrimitiveType fromSwaggerValue(String type) {
        return PrimitiveType.valueOf(type.toUpperCase());
    }
}
