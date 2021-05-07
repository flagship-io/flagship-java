package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;
import java.util.concurrent.CompletableFuture;

/**
 * Visitor method strategy to use when the SDK status is not yet READY.
 */
class NotReadyStrategy extends DefaultStrategy {

    // Call default updateContext

    @Override
    public CompletableFuture<Visitor> synchronizeModifications(Visitor visitor) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.SYNCHRONIZE, "synchronizeModifications()");
        return CompletableFuture.completedFuture(visitor);
    }

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
}
