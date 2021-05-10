package com.abtasty.flagship.main.visitor;


import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;
import java.util.HashMap;

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class PanicStrategy extends DefaultStrategy {

    @Override
    public void updateContext(Visitor visitor, HashMap<String, Object> context) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.UPDATE_CONTEXT, "updateContext()");
    }

    @Override
    public <T> void updateContext(Visitor visitor, String key, T value) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.UPDATE_CONTEXT, "updateContext()");
    }

    // Call default strategy synchronizeModifications

    @Override
    public <T> T getModification(Visitor visitor, String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION, "getModification()");
        return defaultValue;
    }

    @Override
    public <T> T getModification(Visitor visitor, String key, T defaultValue, boolean activate) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION, "getModification()");
        return defaultValue;
    }

    @Override
    public JSONObject getModificationInfo(Visitor visitor, String key) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, "getModificationInfo()");
        return null;
    }

    @Override
    public void activateModification(Visitor visitor, String key) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, "activateModification()");
    }

    @Override
    public <T> void sendHit(Visitor visitor, Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING, "sendHit()");
    }

    @Override
    protected void sendContextRequest(Visitor visitor) { }       //do nothing
}
