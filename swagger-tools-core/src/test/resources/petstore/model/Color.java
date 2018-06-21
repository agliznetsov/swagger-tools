package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Color {
    @JsonProperty("Black")
    BLACK,

    @JsonProperty("White")
    WHITE,

    @JsonProperty("Red")
    RED
}
