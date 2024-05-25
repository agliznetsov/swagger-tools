package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(
        callSuper = true
)
@EqualsAndHashCode(
        callSuper = true
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dog extends Pet {
    @JsonProperty("packSize")
    private Integer packSize = 0;

    public Integer getPackSize() {
        return packSize;
    }

    public void setPackSize(Integer packSize) {
        this.packSize = packSize;
    }
}
