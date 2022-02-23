package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class NoConsentStrategy extends DefaultStrategy {

    public NoConsentStrategy(VisitorDelegate visitor) {
        super(visitor);
    }

    // Call default updateContext

    protected void logMethodDeactivatedError(FlagshipLogManager.Tag tag, String visitorId, String methodName) {
        FlagshipLogManager.log(tag, LogManager.Level.ERROR,
                String.format(FlagshipConstants.Errors.METHOD_DEACTIVATED_CONSENT_ERROR, methodName, visitorId));
    }
//
//    @Override
//    public <T> T getModification(String key, T defaultValue, boolean activate) {
//        if (activate)
//            logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, visitorDelegate.visitorId, "activateModification()");
//        return super.getModification(key, defaultValue, false);
//    }
//
//    @Override
//    public void activateModification(String key) {
//        logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE,  visitorDelegate.visitorId,"activateModification()");
//    }

    @Override
    public <T> void exposeFlag(String key, T defaultValue) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.FLAG_USER_EXPOSED, visitorDelegate.getVisitorId(), "Flag.userExposed()");
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING,  visitorDelegate.getVisitorId(),"sendHit()");
    }

    @Override
    public void sendContextRequest() {} //do nothing

    @Override
    public void cacheVisitor() {} //do nothing

    @Override
    public void lookupVisitorCache() {} //do nothing

    @Override
    public void lookupHitCache() {} //do nothing

    @Override
    public void cacheHit(String visitorId, JSONObject data) {
        if (data.has("data")) {
            JSONObject jsonData = data.getJSONObject("data");
            if (jsonData.has("content")) {
                JSONObject contentData = jsonData.getJSONObject("content");
                if (contentData.optString("ea").equals("fs_consent")) {
                    super.cacheHit(visitorId, data);
                }
            }
        }
        // else do nothing
    }
}