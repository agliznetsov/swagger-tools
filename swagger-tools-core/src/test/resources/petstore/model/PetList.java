package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.util.List;

public class PetList {
    @JsonProperty("_items")
    List<Pet> items;

    @JsonProperty("_max")
    Integer max;

    @JsonProperty("_offset")
    Integer offset;

    public List<Pet> getItems() {
        return items;
    }

    public void setItems(List<Pet> items) {
        this.items = items;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
