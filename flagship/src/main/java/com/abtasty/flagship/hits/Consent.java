package com.abtasty.flagship.hits;

public class Consent extends Event {

    public Consent(boolean consent) {
        super(EventCategory.USER_ENGAGEMENT, "fs_consent");
        withEventLabel("java:"+consent);
    }

    @Override
    public boolean checkData() {
        return true;
    }
}
