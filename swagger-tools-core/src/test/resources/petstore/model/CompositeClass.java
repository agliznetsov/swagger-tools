package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.String;

public class CompositeClass {
    @JsonProperty("name")
    String name;

    @JsonProperty("details")
    CompositeClassDetails details;

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