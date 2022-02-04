package com.abtasty.flagship.model;

import com.abtasty.flagship.visitor.VisitorDelegate;

public class Flag<T> {

    private final VisitorDelegate visitorDelegate;
    private final String          key;
    private final T               defaultValue;

    public Flag(VisitorDelegate visitorDelegate, String key, T defaultValue) {
        this.visitorDelegate = visitorDelegate;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public T value(Boolean userExposed) {
        Object value = visitorDelegate.getStrategy().getFlagValue(key, defaultValue);
        if (userExposed)
            userExposed();
        return (T) value;
    }

    public FlagMetadata metadata() {
        return FlagMetadata.fromModification(visitorDelegate.getStrategy().getFlagMetadata(key, defaultValue));
    }

    public void userExposed() {
        visitorDelegate.getStrategy().exposeFlag(key, defaultValue);
    }

    public boolean exists() {
        return metadata().exists();
    }
}
