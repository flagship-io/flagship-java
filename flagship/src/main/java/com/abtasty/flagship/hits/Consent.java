package com.abtasty.flagship.hits;

public class Consent extends Event {

    public Consent(boolean consent) {
        super(EventCategory.USER_ENGAGEMENT, "fs_content");
        withEventLabel("java:"+consent);
    }

    @Override
    public boolean checkData() {
        return true;
    }
}
