package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.model.Modification;
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
//
//    @Override
//    public CompletableFuture<Visitor> synchronizeModifications() {
//        logMethodDeactivatedError(FlagshipLogManager.Tag.SYNCHRONIZE, "synchronizeModifications()");
//        return CompletableFuture.completedFuture(visitorDelegate.originalVisitor);
//    }
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
    public CompletableFuture<Visitor> fetchFlags() {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAGS_FETCH, "fetchFlags()");
        return CompletableFuture.completedFuture(visitorDelegate.originalVisitor);
    }

    @Override
    public <T> T getFlagValue(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_VALUE, "Flag.value()");
        return defaultValue;
    }


    @Override
    public <T> Modification getFlagMetadata(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_METADATA, "Flag.metadata()");
        return null;
    }

    @Override
    public <T> void exposeFlag(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_USER_EXPOSED, "Flag.userExposed()");
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING, "sendHit()");
    }

    @Override
    public void sendContextRequest() { }//do nothing

    //call default sendConsent

    // call default authenticate
    // call default unauthenticate

    @Override
    public void cacheVisitor() {} //do nothing

    @Override
    public void lookupVisitorCache() {} //do nothing

    @Override
    public void lookupHitCache() {} //do nothing

    @Override
    public void cacheHit(String visitorId, JSONObject data) {} //do nothing
}
