package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.String;

public class Owner {
    @JsonProperty("name")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
