package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;

public class Dog extends Pet {
    @JsonProperty("packSize")
    Integer packSize = 0;

    public Integer getPackSize() {
        return packSize;
    }

    public void setPackSize(Integer packSize) {
        this.packSize = packSize;
    }
}
