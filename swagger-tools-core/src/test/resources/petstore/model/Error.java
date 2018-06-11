package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class Error extends HashMap<String, String> {
    @JsonProperty("code")
    Integer code;

    @JsonProperty("message")
    String message;

    @JsonProperty("value")
    Object value;

    @JsonProperty("details")
    Map<String, Object> details = new HashMap<String, Object>();

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}