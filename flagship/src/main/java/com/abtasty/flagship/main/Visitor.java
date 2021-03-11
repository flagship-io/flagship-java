package com.abtasty.flagship.main;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Flagship visitor representation.
 */
public class Visitor {

    private String                          visitorId;
    private FlagshipConfig                  config;
    private HashMap<String, Object>         context = new HashMap<>();
    private HashMap<String, Modification>   modifications = new HashMap<>();
    private DecisionManager                 decisionManager;
    private TrackingManager                 trackingManager;

    /**
     * Create a new visitor.
     * @param config configuration used when the visitor has been created.
     * @param visitorId visitor unique identifier.
     * @param context visitor context.
     */
    protected Visitor(FlagshipConfig config, String visitorId, HashMap<String, Object> context) {
        this.config = config;
        this.decisionManager = config.getDecisionManager();
        this.trackingManager = config.getTrackingManager();
        this.visitorId = visitorId;
        this.updateContext(context);
    }

    public HashMap<String, Object> getContext() {
        return this.context;
    }

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     *
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context keys must be String, and values types must be one of the following : Number, Boolean, String.
     *
     * @param context: HashMap of keys, values.
     */
    public void updateContext(HashMap<String, Object> context) {
        this.updateContext(context, null);
    }

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     *
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context keys must be String, and values types must be one of the following : Number, Boolean, String.
     *
     * @param context: HashMap of keys, values.
     * @param onSynchronize: If set, the SDK will automatically call
     *         synchronizeModifications() and then update the modifications from the server for all campaigns
     *         according to the updated current visitor context. You can also update it manually later with :
     *         synchronizeModifications()
     */
    public void updateContext(HashMap<String, Object> context, OnSynchronizedListener onSynchronize) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                this.updateContextValue(e.getKey(), e.getValue(), onSynchronize);
            }
        }
        this.logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     *
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context key must be String, and value type must be one of the following : Number, Boolean, String.
     *
     * @param key: context key.
     * @param value context value.
     */
    public <T> void updateContext(String key, T value) {
        this.updateContext(key, value, null);
    }

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     *
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context key must be String, and value type must be one of the following : Number, Boolean, String.
     *
     * @param key: context key.
     * @param value context value.
     * @param onSynchronize: If set, the SDK will automatically call
     *         synchronizeModifications() and then update the modifications from the server for all campaigns
     *         according to the updated current visitor context. You can also update it manually later with :
     *         synchronizeModifications()
     */
    public <T> void updateContext(String key, T value, OnSynchronizedListener onSynchronize) {
        this.updateContextValue(key, value, onSynchronize);
    }

    private void updateContextValue(String key, Object value, OnSynchronizedListener onSynchronize) {
        if (!this.decisionManager.isPanic()) {
            if (key != null && value != null &&
                    (value instanceof String || value instanceof Number || value instanceof Boolean ||
                            value instanceof JSONObject || value instanceof JSONArray)) {
                this.context.put(key, value);
            } else
                FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, Level.WARNING, FlagshipConstants.Errors.CONTEXT_PARAM_ERROR);
            if (onSynchronize != null)
                synchronizeModifications(onSynchronize);
        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.UPDATE_CONTEXT, Level.SEVERE, String.format(FlagshipConstants.Errors.PANIC_ERROR, "updateContext()"));
    }

    /**
     * Synchronization ready callback.
     */
    public interface OnSynchronizedListener {
        void onSynchronized();
    }

    /**
     *  This function will call the decision api and update all the campaigns modifications from the server according to the visitor context.
     *  @param onSynchronize synchronize callback
     */
    public void synchronizeModifications(OnSynchronizedListener onSynchronize) {
        CompletableFuture.runAsync(() -> {
            try {
                ArrayList<Campaign> campaigns = this.decisionManager.getCampaigns(this.config.getEnvId(), visitorId, context);
                this.modifications.clear();
                if (!decisionManager.isPanic()) {
                    HashMap<String, Modification> modifications = this.decisionManager.getModifications(this.config.getDecisionMode(), campaigns);
                    if (modifications != null)
                        this.modifications.putAll(modifications);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).whenCompleteAsync((Void, error) -> {
            logVisitor(FlagshipLogManager.Tag.SYNCHRONIZE);
            if (onSynchronize != null)
                onSynchronize.onSynchronized();
        });
    }

    private void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, toString());
        FlagshipLogManager.log(tag, Level.INFO, visitorStr);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key, default value will be returned.
     *
     * @param key key associated to the modification.
     * @param defaultValue default value to return.
     * @return modification value or default value.
     */
    public <T> T getModification(String key, T defaultValue) {
        return this.getModification(key, defaultValue, false);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key or if the stored value type and default value type do not match, default value will be returned.
     *
     * @param key key associated to the modification.
     * @param defaultValue default value to return.
     * @param activate Set this parameter to true to automatically report on our server that the
     *         current visitor has seen this modification. It is possible to call activateModification() later.
     * @return modification value or default value.
     */
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        if (!decisionManager.isPanic()) {
            try {
                if (key == null) {
                    FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, Level.SEVERE, String.format(FlagshipConstants.Errors.GET_MODIFICATION_KEY_ERROR, key));
                } else if (!this.modifications.containsKey(key)) {
                    FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, Level.SEVERE, String.format(FlagshipConstants.Errors.GET_MODIFICATION_MISSING_ERROR, key));
                } else {
                    Modification modification = this.modifications.get(key);
                    Object castValue = ((T) modification.getValue());
                    if (defaultValue == null || castValue == null || castValue.getClass().equals(defaultValue.getClass())) {
                        if (activate)
                            activateModification(modification);
                        return (T) castValue;
                    } else
                        FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, Level.SEVERE, String.format(FlagshipConstants.Errors.GET_MODIFICATION_CAST_ERROR, key));
                }
            } catch (Exception e) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, Level.SEVERE, String.format(FlagshipConstants.Errors.GET_MODIFICATION_ERROR, key));
            }
        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION, Level.SEVERE, String.format(FlagshipConstants.Errors.PANIC_ERROR, "getModiciation()"));
        return defaultValue;
    }

    /**
     * Get the campaign modification information value matching the given key.
     * @param key key which identify the modification.
     * @return JSONObject containing the modification information.
     */
    public JSONObject getModificationInfo(String key) {
        if (key == null || !this.modifications.containsKey(key)) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.GET_MODIFICATION_INFO, Level.SEVERE, String.format(FlagshipConstants.Errors.GET_MODIFICATION_INFO_ERROR, key));
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
     * @param key key which identify the modification to activate.
     */
    public void activateModification(String key) {
        if (!decisionManager.isPanic())
            this.getModification(key, null, true);
        else
            FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, Level.SEVERE, String.format(FlagshipConstants.Errors.PANIC_ERROR, "activateModification()"));
    }

    private void activateModification(Modification modification) {
        if (modification != null)
            this.sendHit(new Activate(modification));
    }

    /**
     * Send a Hit to Flagship servers for reporting.
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
            FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, Level.SEVERE, String.format(FlagshipConstants.Errors.PANIC_ERROR, "sendHit()"));
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
            modificationJson.put(flag, modification.getValue());
        });
        json.put("context", contextJson);
        json.put("modifications", modificationJson);
        return json.toString(2);
    }
}
