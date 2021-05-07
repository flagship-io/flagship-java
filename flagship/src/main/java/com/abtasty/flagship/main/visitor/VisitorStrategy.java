package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

abstract class VisitorStrategy {

    abstract void                       updateContext(Visitor visitor, HashMap<String, Object> context);
    abstract <T> void                   updateContext(Visitor visitor, String key, T value);
    abstract CompletableFuture<Visitor> synchronizeModifications(Visitor visitor);
    abstract <T> T                      getModification(Visitor visitor, String key, T defaultValue);
    abstract <T> T                      getModification(Visitor visitor, String key, T defaultValue, boolean activate);
    abstract JSONObject                 getModificationInfo(Visitor visitor, String key);
    abstract void                       activateModification(Visitor visitor, String key);
    abstract <T> void                   sendHit(Visitor visitor, Hit<T> hit);

    protected void logMethodDeactivatedError(FlagshipLogManager.Tag tag, String methodName) {
        FlagshipLogManager.log(tag, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.METHOD_DEACTIVATED_ERROR, methodName, Flagship.getStatus()));
    }
}