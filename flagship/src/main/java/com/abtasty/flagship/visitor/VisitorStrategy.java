package com.abtasty.flagship.visitor;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;

import java.util.HashMap;

public abstract class VisitorStrategy implements IVisitor {

    protected VisitorDelegate   visitorDelegate;
    protected FlagshipConfig<?> flagshipConfig;

    public VisitorStrategy(VisitorDelegate visitorDelegate) {
        this.visitorDelegate = visitorDelegate;
        this.flagshipConfig = visitorDelegate.getConfigManager().getFlagshipConfig();
    }

    protected void logMethodDeactivatedError(FlagshipLogManager.Tag tag, String methodName) {
        FlagshipLogManager.log(tag, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.METHOD_DEACTIVATED_ERROR, methodName, Flagship.getStatus()));
    }

    protected void logCacheException(String message, Exception e) {
        logCacheError(message);
        FlagshipLogManager.exception(e);
    }

    protected void logCacheError(String message) {
        FlagshipLogManager.log(FlagshipLogManager.Tag.CACHE, LogManager.Level.ERROR, message);
    }

    protected void logFlagError(FlagshipLogManager.Tag tag, Exception e, String message) {
        if (e instanceof FlagshipConstants.Exceptions.FlagTypeException)
            message += FlagshipConstants.Errors.FLAG_CAST_ERROR;
        else if (e instanceof FlagshipConstants.Exceptions.FlagNotFoundException)
            message += FlagshipConstants.Errors.FLAG_MISSING_ERROR;
        else
            message += FlagshipConstants.Errors.FLAG_ERROR;
        FlagshipLogManager.log(tag , LogManager.Level.ERROR, message);
    }

    public abstract void sendContextRequest();

    abstract void sendConsentRequest();

    abstract void loadContext(HashMap<String, Object> context);

    public abstract void cacheVisitor();

    public abstract void lookupVisitorCache();

    public abstract void flushVisitorCache();

    public abstract void lookupHitCache();

    public abstract void cacheHit(String visitorId, JSONObject data);

    public abstract void flushHitCache();

    public abstract <T> Modification getFlagMetadata(String key, T defaultValue);

    public abstract <T> T getFlagValue(String key, T defaultValue);

    public abstract <T> void exposeFlag(String key, T defaultValue);
}