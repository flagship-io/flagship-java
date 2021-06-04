package com.springboot.model;

import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Visitor {

    @JsonProperty(value = "visitor_id")
    private String visitor_id;

    @JsonProperty(value = "authenticated")
    private Boolean isAuthenticated;

    @JsonProperty(value = "context")
    private HashMap<String, Object> context;

    public Visitor() {

    }

    public Visitor(String visitor_id, Boolean isAuthenticated, HashMap<String, Object> context) {
        this.visitor_id = visitor_id;
        this.isAuthenticated = isAuthenticated;
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

    public Boolean getAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        isAuthenticated = authenticated;
    }

    @Override
    public String toString() {
        return "Visitor [visitor_id=" + visitor_id + ", isAuthenticated=" + isAuthenticated + ", context=" + context + "]";
    }

}
