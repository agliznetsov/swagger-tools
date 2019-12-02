package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetList {
    @JsonProperty("_items")
    private List<Pet> items = new ArrayList<Pet>();

    @JsonProperty("_max")
    private Integer max;

    @JsonProperty("_offset")
    private Integer offset;

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
