package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipLogManager;

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class NoConsentStrategy extends DefaultStrategy {
    @Override
    public <T> T getModification(Visitor visitor, String key, T defaultValue, boolean activate) {
        if (activate)
            logMethodDeactivatedError(FlagshipLogManager.Tag.ACTIVATE, "activateModification()");
        return super.getModification(visitor, key, defaultValue, false);
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