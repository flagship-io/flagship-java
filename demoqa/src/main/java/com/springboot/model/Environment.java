package com.springboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Environment {

    @JsonProperty(value = "environment_id")
    private String environment_id;

    @JsonProperty(value = "api_key")
    private String api_key;

    @JsonProperty(value = "flagship_mode")
    private String flagship_mode;

    @JsonProperty(value = "timeout")
    private int timeout;

    @JsonProperty(value = "polling_interval")
    private int polling_interval;

    @JsonProperty(value = "polling_interval_unit")
    private String polling_interval_unit;

    public Environment() {

    }

    public Environment(String environment_id, String api_key, int timeout, String flagship_mode, int polling_interval, String polling_interval_unit) {
        this.environment_id = environment_id;
        this.api_key = api_key;
        this.timeout = timeout;
        this.flagship_mode = flagship_mode;
        this.polling_interval = polling_interval;
        this.polling_interval_unit = polling_interval_unit;
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

    public String getFlagship_mode() {
        return flagship_mode;
    }

    public void setFlagship_mode(String flagship_mode) {
        this.flagship_mode = flagship_mode;
    }

    public int getPolling_interval() {
        return polling_interval;
    }

    public void setPolling_interval(int polling_interval) {
        this.polling_interval = polling_interval;
    }

    public String getPolling_interval_unit() {
        return polling_interval_unit;
    }

    public void setPolling_interval_unit(String polling_interval_unit) {
        this.polling_interval_unit = polling_interval_unit;
    }

    @Override
    public String toString() {
        return "Environment [environment_id=" + getEnvironment_id() + ", api_key=" + getApi_key() + ", timeout=" + getTimeout()
                + ", flagship_mode=" + getFlagship_mode() + ", if bucketing polling interval " + getPolling_interval() + ", and polling interval unit " + getPolling_interval_unit() + "]";
    }


}
