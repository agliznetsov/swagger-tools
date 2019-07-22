package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Order {
    @JsonProperty("id")
    @NotNull
    @Pattern(
            regexp = "id.+"
    )
    private String id;

    @JsonProperty("name")
    @NotNull
    @Size(
            max = 10,
            min = 3
    )
    private String name;

    @JsonProperty("mumber")
    @Max(10)
    @Min(10)
    private Integer mumber;

    @JsonProperty("description")
    private String description;

    @JsonProperty("customer")
    @NotNull
    @Valid
    private Customer customer;

    @JsonProperty("items")
    @Valid
    @Size(
            min = 1
    )
    private List<Item> items = new ArrayList<Item>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMumber() {
        return mumber;
    }

    public void setMumber(Integer mumber) {
        this.mumber = mumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}