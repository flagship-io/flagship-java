package com.abtasty.flagship.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import com.sun.org.apache.xpath.internal.operations.Mod;
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

    public HashMap<String, Object> getContext() {
        return context;
    }

    public void updateContext(HashMap<String, Object> context) {
        if (context != null) {
            for (HashMap.Entry<String, Object> e : context.entrySet()) {
                updateContextValue(e.getKey(), e.getValue());
            }
        }
        logVisitor();
    }

    public void updateContext(String key, Number value) {
        updateContextValue(key, value);
    }

    public void updateContext(String key, Boolean value) {
        updateContextValue(key, value);
    }

    public void updateContext(String key, String value) {
        updateContextValue(key, value);
    }

    public void updateContext(String key, JSONObject value) {
        updateContextValue(key, value);
    }

    public void updateContext(String key, JSONArray value) {
        updateContextValue(key, value);
    }

    private void updateContextValue(String key, Object value) {
        if (key != null && value != null &&
                (value instanceof String || value instanceof Number || value instanceof Boolean ||
                        value instanceof JSONObject || value instanceof JSONArray)) {
            this.context.put(key, value);
        } else {
            config.logManager.onLog(LogManager.Tag.UPDATE_CONTEXT, LogLevel.WARNING, FlagshipConstants.CONTEXT_PARAM_ERROR);
        }
    }

    public interface OnSynchronizedListener {
        void onSynchronized();
    }

    public void synchronizeModifications(OnSynchronizedListener listener) {
        CompletableFuture.runAsync(() -> {
            try {
                ArrayList<Campaign> campaigns = this.decisionManager.getCampaigns(visitorId, context);
                this.modifications.clear();
                this.modifications.putAll(this.decisionManager.getModifications(campaigns));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).whenCompleteAsync((Void, error) -> {
            if (listener != null)
                listener.onSynchronized();
        });
    }

    private void logVisitor() {
        String visitorStr = String.format(FlagshipConstants.VISITOR, visitorId, toString());
        config.logManager.onLog(LogManager.Tag.UPDATE_CONTEXT, LogLevel.INFO, visitorStr);
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    private JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("Visitor id", visitorId);
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
        return json;
    }

    public void setDecisionManager(DecisionManager decisionManager) {
        this.decisionManager = decisionManager;
    }
}
