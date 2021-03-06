package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompositeClass {
    @JsonProperty("name")
    private String name;

    @JsonProperty("details")
    private CompositeClassDetails details;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CompositeClassDetails getDetails() {
        return details;
    }

    public void setDetails(CompositeClassDetails details) {
        this.details = details;
    }
}
