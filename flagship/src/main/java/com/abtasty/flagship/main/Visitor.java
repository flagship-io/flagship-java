package com.abtasty.flagship.main;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Flagship visitor representation.
 */
public class Visitor {

    private final String                                visitorId;
    private final ConcurrentMap<String, Object>         context = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Modification>   modifications = new ConcurrentHashMap<>();
    private final DecisionManager                       decisionManager;
    private final TrackingManager                       trackingManager;

    /**
     * Create a new visitor.
     *
     * @param config    configuration used when the visitor has been created.
     * @param visitorId visitor unique identifier.
     * @param context   visitor context.
     */
    protected Visitor(FlagshipConfig config, DecisionManager decisionManager, String visitorId, HashMap<String, Object> context) {
        this.decisionManager = decisionManager;
        this.trackingManager = config.getTrackingManager();
        this.visitorId = visitorId;
        this.updateContext(context);
    }

    public ConcurrentMap<String, Object> getContext() {
        return this.context;
    }

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context keys must be String, and values types must be one of the following : Number, Boolean, String.
     *
     * @param context:       HashMap of keys, values.
     */
    public void updateContext(HashMap<String, Object> context) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                this.updateContext(e.getKey(), e.getValue());
            }
        }
        this.logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }


    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context key must be String, and value type must be one of the following : Number, Boolean, String.
     *
     * @param key:           context key.
     * @param value          context value.
     */
    public <T> void updateContext(String key, T value) {
        if (!this.decisionManager.isPanic()) {
            if (key != null && (value instanceof String || value instanceof Number || value instanceof Boolean ||
                            value instanceof JSONObject || value instanceof JSONArray)) {
                this.context.put(key, value);
            } else
                FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.WARNING, FlagshipConstants.Errors.CONTEXT_PARAM_ERROR);

        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "updateContext()"));
    }



    /**
     * This function will call the decision api and update all the campaigns modifications from the server according to the visitor context.
     * @return a CompletableFuture for this synchronization
     */
    public CompletableFuture<Visitor> synchronizeModifications() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ArrayList<Campaign> campaigns = this.decisionManager.getCampaigns(visitorId, new HashMap<String, Object>(context));
                this.modifications.clear();
                if (!decisionManager.isPanic()) {
                    HashMap<String, Modification> modifications = this.decisionManager.getModifications(campaigns);
                    if (modifications != null)
                        this.modifications.putAll(modifications);
                }
            } catch (Exception e) {
                FlagshipLogManager.exception(e);
            }
            return this;
        }, HttpManager.getInstance().getThreadPoolExecutor()).whenCompleteAsync((instance, error) -> {
            logVisitor(FlagshipLogManager.Tag.SYNCHRONIZE);
        });
    }

    private void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, toString());
        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key, default value will be returned.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @return modification value or default value.
     */
    public <T> T getModification(String key, T defaultValue) {
        return this.getModification(key, defaultValue, false);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key or if the stored value type and default value type do not match, default value will be returned.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @param activate     Set this parameter to true to automatically report on our server that the
     *                     current visitor has seen this modification. It is possible to call activateModification() later.
     * @return modification value or default value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        if (!decisionManager.isPanic()) {
            try {
                if (key == null) {
                    FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_KEY_ERROR, key));
                } else if (!this.modifications.containsKey(key)) {
                    FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_MISSING_ERROR, key));
                } else {
                    Modification modification = this.modifications.get(key);
                    T castValue = (T) ((modification.getValue() != null) ? modification.getValue() : defaultValue);
                    if (defaultValue == null || castValue == null || castValue.getClass().equals(defaultValue.getClass())) {
                        if (activate)
                            activateModification(modification);
                        return castValue;
                    } else
                        FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_CAST_ERROR, key));
                }
            } catch (Exception e) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_ERROR, key));
            }
        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "getModification()"));
        return defaultValue;
    }

    /**
     * Get the campaign modification information value matching the given key.
     *
     * @param key key which identify the modification.
     * @return JSONObject containing the modification information.
     */
    public JSONObject getModificationInfo(String key) {
        if (key == null || !this.modifications.containsKey(key)) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_INFO_ERROR, key));
            return null;
        } else {
            JSONObject obj = new JSONObject();
            Modification modification = this.modifications.get(key);
            obj.put("campaignId", modification.getCampaignId());
            obj.put("variationGroupId", modification.getVariationGroupId());
            obj.put("variationId", modification.getVariationId());
            obj.put("isReference", modification.isReference());
            return obj;
        }
    }

    /**
     * Report this user has seen this modification.
     *
     * @param key key which identify the modification to activate.
     */
    public void activateModification(String key) {
        if (!decisionManager.isPanic())
            this.getModification(key, null, true);
        else
            FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "activateModification()"));
    }

    private void activateModification(Modification modification) {
        if (modification != null)
            this.sendHit(new Activate(modification));
    }

    /**
     * Send a Hit to Flagship servers for reporting.
     *
     * @param hit hit to track.
     */
    public void sendHit(Hit hit) {
        if (!decisionManager.isPanic() && this.trackingManager != null) {
            if (hit != null && hit.checkData()) {
                if (hit instanceof Activate)
                    trackingManager.sendActivation(visitorId, (Activate) hit);
                else
                    trackingManager.sendHit(visitorId, hit);
            }
        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "sendHit()"));
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("visitorId", visitorId);
        JSONObject contextJson = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            contextJson.put(e.getKey(), e.getValue());
        }
        JSONObject modificationJson = new JSONObject();
        this.modifications.forEach((flag, modification) -> {
            Object value = modification.getValue();
            modificationJson.put(flag, (value == null) ? JSONObject.NULL : value);
        });
        json.put("context", contextJson);
        json.put("modifications", modificationJson);
        return json.toString(2);
    }
}
