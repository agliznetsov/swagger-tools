package com.example;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "@type",
        visible = false
)
@JsonSubTypes(@JsonSubTypes.Type(value = AnimalImpl.class, name = "AnimalImpl"))
public class Animal {
}