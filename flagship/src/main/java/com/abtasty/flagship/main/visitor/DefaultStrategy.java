package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Visitor default method strategy
 */
class DefaultStrategy extends VisitorStrategy {

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

    protected void sendContextRequest(Visitor visitor) {
        ConfigManager configManager = visitor.getManagerConfig();
        TrackingManager trackingManager = configManager.getTrackingManager();
        trackingManager.sendContextRequest(configManager.getFlagshipConfig().getEnvId(), visitor.visitorId, visitor.getContext());
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications(Visitor visitor) {
        DecisionManager decisionManager = visitor.getManagerConfig().getDecisionManager();
        return CompletableFuture.supplyAsync(() -> {
            try {
                HashMap<String, Modification> modifications = decisionManager.getCampaignsModifications(visitor);
                if (modifications != null) {
                    visitor.modifications.clear();
                    visitor.modifications.putAll(modifications);
                }
                sendContextRequest(visitor);
            } catch (Exception e) {
                FlagshipLogManager.exception(e);
            }
            return visitor;
        }, HttpManager.getInstance().getThreadPoolExecutor()).whenCompleteAsync((instance, error) -> visitor.logVisitor(FlagshipLogManager.Tag.SYNCHRONIZE));
    }

    @Override
    public <T> T getModification(Visitor visitor, String key, T defaultValue) {
        return getModification(visitor, key, defaultValue, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T getModification(Visitor visitor, String key, T defaultValue, boolean activate) {
        try {
            if (key == null) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_KEY_ERROR, "null"));
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
        TrackingManager trackingManager = visitor.getManagerConfig().getTrackingManager();
        if (trackingManager != null) {
            if (hit != null && hit.checkData()) {
                if (hit instanceof Activate)
                    trackingManager.sendActivation(visitor.visitorId, (Activate) hit);
                else
                    trackingManager.sendHit(visitor.visitorId, hit);
            }
        }
    }
}