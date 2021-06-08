package com.abtasty.flagship.visitor;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * Visitor default method strategy
 */
class DefaultStrategy extends VisitorStrategy {

    public DefaultStrategy(VisitorDelegate visitor) {
        super(visitor);
    }

    @Override
    public void updateContext(HashMap<String, Object> context) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                this.updateContext(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public <T> void updateContext(String key, T value) {
        if (key == null)
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, FlagshipConstants.Errors.CONTEXT_KEY_ERROR);
//        else if (((value instanceof String) || (value instanceof Number) || (value instanceof Boolean) || (value instanceof JSONObject) || (value instanceof JSONArray)))
        else if (!((value instanceof String) || (value instanceof Number) || (value instanceof Boolean)))
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.CONTEXT_VALUE_ERROR, key));
        else if (FlagshipContext.isReserved(key))
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.CONTEXT_RESERVED_KEY_ERROR, key));
        else
            visitorDelegate.getVisitorContext().put(key, value);
    }

    @Override
    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        if (flagshipContext.verify(value))
            visitorDelegate.getVisitorContext().put(flagshipContext.key(), value);
    }

    @Override
    public void clearContext() {
        visitorDelegate.getVisitorContext().clear();
        visitorDelegate.loadContext(null);
    }

    @Override
    protected void sendContextRequest() {
        ConfigManager configManager = visitorDelegate.getConfigManager();
        TrackingManager trackingManager = configManager.getTrackingManager();
        trackingManager.sendContextRequest(configManager.getFlagshipConfig().getEnvId(), visitorDelegate.getId(), visitorDelegate.getContext());
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications() {

        DecisionManager decisionManager = visitorDelegate.getConfigManager().getDecisionManager();
        return CompletableFuture.supplyAsync(() -> {
            try {
                visitorDelegate.updateModifications(decisionManager.getCampaignsModifications(visitorDelegate));
            } catch (Exception e) {
                FlagshipLogManager.exception(e);
            }
            return visitorDelegate.getOriginalVisitor();
        }, HttpManager.getInstance().getThreadPoolExecutor()).whenCompleteAsync((instance, error) -> visitorDelegate.logVisitor(FlagshipLogManager.Tag.SYNCHRONIZE));
    }

    @Override
    public <T> T getModification(String key, T defaultValue) {
        return getModification(key, defaultValue, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        ConcurrentMap<String, Modification> visitorModifications = visitorDelegate.getVisitorModifications();
        try {
            if (key == null) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_KEY_ERROR, "null"));
            } else if (!visitorModifications.containsKey(key)) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_MISSING_ERROR, key));
            } else {
                Modification modification = visitorModifications.get(key);
                T castValue = (T) ((modification.getValue() != null) ? modification.getValue() : defaultValue);
                if (defaultValue == null || castValue == null || castValue.getClass().equals(defaultValue.getClass())) {
                    if (activate)
                        this.activateModification(modification);
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
    public JSONObject getModificationInfo(String key) {
        ConcurrentMap<String, Modification> visitorModifications = visitorDelegate.getVisitorModifications();
        if (key == null || !visitorModifications.containsKey(key)) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_INFO_ERROR, key));
            return null;
        } else {
            JSONObject obj = new JSONObject();
            Modification modification = visitorModifications.get(key);
            obj.put("campaignId", modification.getCampaignId());
            obj.put("variationGroupId", modification.getVariationGroupId());
            obj.put("variationId", modification.getVariationId());
            obj.put("isReference", modification.isReference());
            return obj;
        }
    }

    private void activateModification(Modification modification) {
        if (modification != null)
            this.sendHit(new Activate(modification));
    }

    @Override
    public void activateModification(String key) {
        this.getModification(key, null, true);
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        TrackingManager trackingManager = visitorDelegate.getConfigManager().getTrackingManager();
        if (trackingManager != null && hit != null)
            trackingManager.sendHit(visitorDelegate, hit);
    }

    @Override
    public void authenticate(String visitorId) {
        if (visitorDelegate.getConfigManager().isDecisionMode(Flagship.DecisionMode.API)) {
            if (visitorDelegate.getAnonymousId() == null)
                visitorDelegate.setAnonymousId(visitorDelegate.getId());
            visitorDelegate.setId(visitorId);
        } else {
            FlagshipLogManager.log(FlagshipLogManager.Tag.AUTHENTICATE, LogManager.Level.ERROR,
                    String.format(FlagshipConstants.Errors.AUTHENTICATION_BUCKETING_ERROR, "authenticate"));
        }
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void unauthenticate() {
        if (visitorDelegate.getConfigManager().isDecisionMode(Flagship.DecisionMode.API)) {
            if (visitorDelegate.getAnonymousId() != null) {
                visitorDelegate.setId(visitorDelegate.getAnonymousId());
                visitorDelegate.setAnonymousId(null);
            }
        } else {
            FlagshipLogManager.log(FlagshipLogManager.Tag.UNAUTHENTICATE, LogManager.Level.ERROR,
                    String.format(FlagshipConstants.Errors.AUTHENTICATION_BUCKETING_ERROR, "unauthenticate"));
        }
    }
}