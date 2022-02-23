package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.model.Modification;
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
//
//    @Override
//    public <T> T getModification(String key, T defaultValue) {
//        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION, "getModification()");
//        return defaultValue;
//    }
//
//    @Override
//    public <T> T getModification(String key, T defaultValue, boolean activate) {
//        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION, "getModification()");
//        return defaultValue;
//    }
//
//    @Override
//    public JSONObject getModificationInfo(String key) {
//        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, "getModificationInfo()");
//        return null;
//    }
//
//    @Override
//    public void activateModification(String key) {
//        logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, "activateModification()");
//    }


    @Override
    public synchronized <T> T getFlagValue(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_VALUE, "Flag.value()");
        return defaultValue;
    }

    @Override
    public synchronized <T> Modification getFlagMetadata(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_METADATA, "Flag.metadata()");
        return null;
    }

    @Override
    public synchronized <T> void exposeFlag(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_USER_EXPOSED, "Flag.userExposed()");
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING, "sendHit()");
    }

    @Override
    public void sendContextRequest() { }       //do nothing

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

    @Override
    public void sendConsentRequest() {} // do nothing

    @Override
    public void setConsent(Boolean hasConsented) {
        visitorDelegate.setConsent(hasConsented);
        logMethodDeactivatedError(FlagshipLogManager.Tag.CONSENT, "setConsent()");
    }

    @Override
    public void cacheVisitor() {} //do nothing

    @Override
    public void lookupVisitorCache() {} //do nothing

    @Override
    public void lookupHitCache() {} //do nothing

    @Override
    public void cacheHit(String visitorId, JSONObject data) {} //do nothing
}
