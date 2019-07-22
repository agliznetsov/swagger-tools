package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.String;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Customer {
    @JsonProperty("name")
    @NotNull
    @Size(
            min = 3
    )
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}