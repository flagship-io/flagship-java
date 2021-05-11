package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;
import java.util.concurrent.CompletableFuture;

/**
 * Visitor method strategy to use when the SDK status is not yet READY.
 */
class NotReadyStrategy extends DefaultStrategy {

    public NotReadyStrategy(VisitorDelegate visitor) {
        super(visitor);
    }

    // Call default updateContext

    @Override
    public CompletableFuture<Visitor> synchronizeModifications() {
        logMethodDeactivatedError(FlagshipLogManager.Tag.SYNCHRONIZE, "synchronizeModifications()");
        return CompletableFuture.completedFuture(visitorDelegate.getOriginalVisitor());
    }

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
}
