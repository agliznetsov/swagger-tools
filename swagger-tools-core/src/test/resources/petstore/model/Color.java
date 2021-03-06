package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Color {
    @JsonProperty("Black")
    BLACK,

    @JsonProperty("White")
    WHITE,

    @JsonProperty("Red")
    RED
}
