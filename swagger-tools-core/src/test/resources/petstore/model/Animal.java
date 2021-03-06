package com.example.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "@type",
        visible = false
)
@JsonSubTypes(@JsonSubTypes.Type(value = AnimalImpl.class, name = "AnimalImpl"))
public class Animal {
}
