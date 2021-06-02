package com.springboot.model;

import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Visitor {

    @JsonProperty(value = "visitor_id")
    private String visitor_id;

    @JsonProperty(value = "context")
    private HashMap<String, Object> context;

    public Visitor() {

    }

    public Visitor(String visitor_id, HashMap<String, Object> context) {
        this.visitor_id = visitor_id;
        this.context = context;
    }

    public String getVisitor_id() {
        return visitor_id;
    }

    public void setVisitor_id(String visitor_id) {
        this.visitor_id = visitor_id;
    }

    public HashMap<String, Object> getContext() {
        return context;
    }

    public void setContext(HashMap<String, Object> context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Visitor [visitor_id=" + visitor_id + ", context=" + context + "]";
    }

}
