package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.String;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(
        builderMethodName = "customerBuilder"
)
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
