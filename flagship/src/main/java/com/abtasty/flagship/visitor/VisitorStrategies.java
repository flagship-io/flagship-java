package com.abtasty.flagship.visitor;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

abstract class BaseStrategy {
    abstract void                        updateContext(Visitor visitor, HashMap<String, Object> context);
    abstract <T> void                    updateContext(Visitor visitor, String key, T value);
    abstract CompletableFuture<Visitor>  synchronizeModifications(Visitor visitor);
    abstract <T> T                       getModification(Visitor visitor, String key, T defaultValue);
    abstract <T> T                       getModification(Visitor visitor, String key, T defaultValue, boolean activate);
    abstract JSONObject                  getModificationInfo(Visitor visitor, String key);
    abstract void                        activateModification(Visitor visitor, String key);
    abstract <T> void                    sendHit(Visitor visitor, Hit<T> hit);


    protected void logMethodDeactivatedError(FlagshipLogManager.Tag tag, String methodName) {
        FlagshipLogManager.log(tag, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.METHOD_DEACTIVATED_ERROR, methodName, Flagship.getStatus()));
    }
}

/**
 * Visitor default method strategy
 */
class VisitorStrategy extends BaseStrategy {

    @Override
    public void updateContext(Visitor visitor, HashMap<String, Object> context) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                this.updateContext(visitor, e.getKey(), e.getValue());
            }
        }
        visitor.logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public <T> void updateContext(Visitor visitor, String key, T value) {
        if (key != null && (value instanceof String || value instanceof Number || value instanceof Boolean ||
                value instanceof JSONObject || value instanceof JSONArray)) {
            visitor.context.put(key, value);
        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.WARNING, FlagshipConstants.Errors.CONTEXT_PARAM_ERROR);
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications(Visitor visitor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ArrayList<Campaign> campaigns = visitor.decisionManager.getCampaigns(visitor.visitorId, new HashMap<String, Object>(visitor.context));
                visitor.modifications.clear();
                if (!visitor.decisionManager.isPanic()) {
                    HashMap<String, Modification> modifications = visitor.decisionManager.getModifications(campaigns);
                    if (modifications != null)
                        visitor.modifications.putAll(modifications);
                }
            } catch (Exception e) {
                FlagshipLogManager.exception(e);
            }
            return visitor;
        }, HttpManager.getInstance().getThreadPoolExecutor()).whenCompleteAsync((instance, error) -> {
            visitor.logVisitor(FlagshipLogManager.Tag.SYNCHRONIZE);
        });
    }

    @Override
    public <T> T getModification(Visitor visitor, String key, T defaultValue) {
        return getModification(visitor, key, defaultValue, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T getModification(Visitor visitor, String key, T defaultValue, boolean activate) {
        try {
            if (key == null) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_KEY_ERROR, key));
            } else if (!visitor.modifications.containsKey(key)) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_MISSING_ERROR, key));
            } else {
                Modification modification = visitor.modifications.get(key);
                T castValue = (T) ((modification.getValue() != null) ? modification.getValue() : defaultValue);
                if (defaultValue == null || castValue == null || castValue.getClass().equals(defaultValue.getClass())) {
                    if (activate)
                        this.activateModification(visitor, modification);
                    return castValue;
                } else
                    FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_CAST_ERROR, key));
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_ERROR, key));
        }
        return defaultValue;
    }

    @Override
    public JSONObject getModificationInfo(Visitor visitor, String key) {
        if (key == null || !visitor.modifications.containsKey(key)) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_INFO_ERROR, key));
            return null;
        } else {
            JSONObject obj = new JSONObject();
            Modification modification = visitor.modifications.get(key);
            obj.put("campaignId", modification.getCampaignId());
            obj.put("variationGroupId", modification.getVariationGroupId());
            obj.put("variationId", modification.getVariationId());
            obj.put("isReference", modification.isReference());
            return obj;
        }
    }

    private void activateModification(Visitor visitor, Modification modification) {
        if (modification != null)
            this.sendHit(visitor, new Activate(modification));
    }

    @Override
    public void activateModification(Visitor visitor, String key) {
        this.getModification(visitor, key, null, true);
    }

    @Override
    public <T> void sendHit(Visitor visitor, Hit<T> hit) {
        if (visitor.trackingManager != null) {
            if (hit != null && hit.checkData()) {
                if (hit instanceof Activate)
                    visitor.trackingManager.sendActivation(visitor.visitorId, (Activate) hit);
                else
                    visitor.trackingManager.sendHit(visitor.visitorId, hit);
            }
        }
    }
}

/**
 * Visitor method strategy to use when the SDK status is not yet READY.
 */
class NotReadyStrategy extends VisitorStrategy {

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

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class PanicStrategy extends VisitorStrategy {

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
}

/**
 * Visitor method strategy to use when the SDK status is READY_PANIC_ON.
 */
class NoConsentStrategy extends VisitorStrategy {
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


