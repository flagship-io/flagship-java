package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;
import java.util.HashMap;

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class PanicStrategy extends DefaultStrategy {

    public PanicStrategy(VisitorDelegate visitor) {
        super(visitor);
    }

    @Override
    public void updateContext(HashMap<String, Object> context) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.UPDATE_CONTEXT, "updateContext()");
    }

    @Override
    public <T> void updateContext(String key, T value) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.UPDATE_CONTEXT, "updateContext()");
    }

    @Override
    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.UPDATE_CONTEXT, "updateContext()");
    }

    @Override
    public void clearContext() {
        logMethodDeactivatedError(FlagshipLogManager.Tag.CLEAR_CONTEXT, "clearContext()");
    }

    // Call default strategy synchronizeModifications

    @Override
    public <T> T getModification(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION, "getModification()");
        return defaultValue;
    }

    @Override
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION, "getModification()");
        return defaultValue;
    }

    @Override
    public JSONObject getModificationInfo(String key) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, "getModificationInfo()");
        return null;
    }

    @Override
    public void activateModification(String key) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, "activateModification()");
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING, "sendHit()");
    }

    @Override
    protected void sendContextRequest() { }       //do nothing

    @Override
    protected void loadContext(HashMap<String, Object> context) { } // do nothing

    @Override
    public void authenticate(String visitorId) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.AUTHENTICATE, "authenticate()");
    }

    @Override
    public void unauthenticate() {
        logMethodDeactivatedError(FlagshipLogManager.Tag.UNAUTHENTICATE, "unauthenticate()");
    }
}
