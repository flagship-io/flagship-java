package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class NoConsentStrategy extends DefaultStrategy {


    protected void logMethodDeactivatedError(FlagshipLogManager.Tag tag, String visitorId, String methodName) {
        FlagshipLogManager.log(tag, LogManager.Level.ERROR,
                String.format(FlagshipConstants.Errors.METHOD_DEACTIVATED_CONSENT_ERROR, methodName, visitorId));
    }

    @Override
    public <T> T getModification(Visitor visitor, String key, T defaultValue, boolean activate) {
        if (activate)
            logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, visitor.visitorId, "activateModification()");
        return super.getModification(visitor, key, defaultValue, false);
    }

    @Override
    public void activateModification(Visitor visitor, String key) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE,  visitor.visitorId,"activateModification()");
    }

    @Override
    public <T> void sendHit(Visitor visitor, Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING,  visitor.visitorId,"sendHit()");
    }

    @Override
    protected void sendContextRequest(Visitor visitor) { }       //do nothing
}