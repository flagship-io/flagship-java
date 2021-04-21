package com.springboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Environment {

    @JsonProperty(value = "environment_id")
    private String environment_id;

    @JsonProperty(value = "api_key")
    private String api_key;

    //private Boolean Bucketing;

    @JsonProperty(value = "timeout")
    private int timeout;

    @JsonProperty(value = "polling_interval")
    private int polling_interval;

    public Environment() {

    }

    public Environment(String environment_id, String api_key, int timeout, int polling_interval) {
        this.environment_id = environment_id;
        this.api_key = api_key;
        this.timeout = timeout;
        this.polling_interval = polling_interval;
    }

    public String getEnvironment_id() {
        return environment_id;
    }

    public void setEnvironment_id(String environment_id) {
        this.environment_id = environment_id;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPolling_interval() {
        return polling_interval;
    }

    public void setPolling_interval(int polling_interval) {
        this.polling_interval = polling_interval;
    }

    @Override
    public String toString() {
        return "Environment [environment_id=" + environment_id + ", api_key=" + api_key + ", timeout=" + timeout
                + ", polling_interval=" + polling_interval + "]";
    }


}
