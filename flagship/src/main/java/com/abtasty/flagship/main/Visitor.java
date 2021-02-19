package com.abtasty.flagship.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.*;

public class Visitor {

    private String                          visitorId = null;
    private FlagshipConfig                  config = null;
    private HashMap<String, Object>         context = new HashMap<>();
    private HashMap<String, Modification>   modifications = new HashMap<>();
    private DecisionManager                 decisionManager = null;

    public Visitor(FlagshipConfig config, String visitorId, HashMap<String, Object> context) {
        this.config = config;
        this.visitorId = visitorId;
    }

    protected void setDecisionManager(DecisionManager decisionManager) {
        this.decisionManager = decisionManager;
    }

    public void updateContext(HashMap<String, Object> context) {
        this.updateContext(context, null);
    }
    public void updateContext(HashMap<String, Object> context, OnSynchronizedListener listener) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                this.updateContextValue(e.getKey(), e.getValue(), listener);
            }
        }
        this.logVisitor(LogManager.Tag.UPDATE_CONTEXT);
    }

    public HashMap<String, Object> getContext() {
        return this.context;
    }

    public <T> void updateContext(String key, T value) {
        this.updateContext(key, value, null);
    };

    public <T> void updateContext(String key, T value, OnSynchronizedListener listener) {
        this.updateContextValue(key, value, listener);
    };

    private void updateContextValue(String key, Object value, OnSynchronizedListener listener) {
        if (!decisionManager.isPanic()) {
            if (key != null && value != null &&
                    (value instanceof String || value instanceof Number || value instanceof Boolean ||
                            value instanceof JSONObject || value instanceof JSONArray)) {
                this.context.put(key, value);
            } else
                LogManager.log(LogManager.Tag.UPDATE_CONTEXT, LogLevel.WARNING, FlagshipConstants.Errors.CONTEXT_PARAM_ERROR);
            if (listener != null)
                synchronizeModifications(listener);
        } else
            LogManager.log(LogManager.Tag.UPDATE_CONTEXT, LogLevel.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "updateContext()"));
    }

    public interface OnSynchronizedListener {
        void onSynchronized();
    }

    public void synchronizeModifications(OnSynchronizedListener listener) {
        CompletableFuture.runAsync(() -> {
            try {
                ArrayList<Campaign> campaigns = this.decisionManager.getCampaigns(visitorId, context);
                this.modifications.clear();
                if (!decisionManager.isPanic()) {
                    HashMap<String, Modification> modifications = this.decisionManager.getModifications(campaigns);
                    if (modifications != null)
                        this.modifications.putAll(modifications);
                }
            } catch (Exception e) {
            }
        }).whenCompleteAsync((Void, error) -> {
            logVisitor(LogManager.Tag.SYNCHRONIZE);
            if (listener != null)
                listener.onSynchronized();
        });
    }

    private void logVisitor(LogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, toString());
        LogManager.log(tag, LogLevel.INFO, visitorStr);
    }

    public <T> T getModification(String key, T defaultValue) {
        return this.getModification(key, defaultValue, false);
    }

    public <T> T getModification(String key, T defaultValue, boolean activate) {
        if (!decisionManager.isPanic()) {
            try {
                if (key == null) {
                    LogManager.log(LogManager.Tag.GET_MODIFICATION, LogLevel.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_KEY_ERROR, key));
                } else if (!this.modifications.containsKey(key)) {
                    config.getLogManager().onLog(LogManager.Tag.GET_MODIFICATION, LogLevel.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_MISSING_ERROR, key));
                } else {
                    Modification modification = this.modifications.get(key);
                    Object castValue = ((T) modification.getValue());
                    if (defaultValue == null || castValue == null || castValue.getClass().equals(defaultValue.getClass())) {
                        if (activate)
                            activateModification(modification);
                        return (T) castValue;
                    } else
                        config.getLogManager().onLog(LogManager.Tag.GET_MODIFICATION, LogLevel.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_CAST_ERROR, key));
                }
            } catch (Exception e) {
                config.getLogManager().onLog(LogManager.Tag.GET_MODIFICATION, LogLevel.ERROR, String.format(FlagshipConstants.Errors.GET_MODIFICATION_ERROR, key));
            }
        } else
            LogManager.log(LogManager.Tag.GET_MODIFICATION, LogLevel.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "getModiciation()"));
        return defaultValue;
    }

    public void activateModification(String key) {
        if (!decisionManager.isPanic())
            this.getModification(key, null, true);
        else
            LogManager.log(LogManager.Tag.TRACKING, LogLevel.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "activateModification()"));
    }

    private void activateModification(Modification modification) {
        if (modification != null)
            this.sendHit(new Activate(modification));
    }

    public void sendHit(Hit hit) {
        if (!decisionManager.isPanic()) {
            if (hit != null && hit.checkData())
                config.getTrackingManager().sendHit(visitorId, hit);
        } else
            LogManager.log(LogManager.Tag.TRACKING, LogLevel.ERROR, String.format(FlagshipConstants.Errors.PANIC_ERROR, "sendHit()"));
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
