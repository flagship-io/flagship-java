package com.springboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Context {

    @JsonProperty(value = "key")
    String key;

    @JsonProperty(value = "type")
    String type;

    @JsonProperty(value = "value")
    String value;

    public Context() {
    }

    public Context(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

