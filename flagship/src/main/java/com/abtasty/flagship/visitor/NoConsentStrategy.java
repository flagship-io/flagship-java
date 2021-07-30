package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;

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

    @Override
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        if (activate)
            logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, visitorDelegate.getVisitorId(), "activateModification()");
        return super.getModification(key, defaultValue, false);
    }

    @Override
    public void activateModification(String key) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE,  visitorDelegate.getVisitorId(),"activateModification()");
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        logMethodDeactivatedError(FlagshipLogManager.Tag.TRACKING,  visitorDelegate.getVisitorId(),"sendHit()");
    }

    @Override
    public void sendContextRequest() { }       //do nothing
}