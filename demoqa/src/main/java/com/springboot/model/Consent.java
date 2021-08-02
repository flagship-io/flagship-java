package com.springboot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Consent {

    @JsonProperty(value = "consent")
    boolean consent = true;

    public Consent() {

    }

    public Consent(boolean consent) {
        this.consent = consent;
    }

    public boolean isConsent() {
        return consent;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }
}
