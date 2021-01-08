package com.abtasty.flagship.main;

import java.util.HashMap;

import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.*;

public class Visitor {

    private String visitorId = null;
    private FlagshipConfig config = null;
    private HashMap<String, Object> context = new HashMap<>();
    private DecisionManager decisionManager = null;

    public Visitor(FlagshipConfig config, String visitorId, HashMap<String, Object> context) {
        this.config = config;
        this.visitorId = visitorId;
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

    public void synchronizeModifications() {
        this.decisionManager.getCampaigns(visitorId, context);
    }

    private void logVisitor() {
        String visitorStr = String.format(FlagshipConstants.VISITOR, visitorId, toJSON().toString());
        config.logManager.onLog(LogManager.Tag.UPDATE_CONTEXT, LogLevel.INFO, visitorStr);
    }

    private JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("Visitor id", visitorId);
        JSONObject contextJson = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            contextJson.put(e.getKey(), e.getValue());
        }
        json.put("context", contextJson);
        return json;
    }

    public void setDecisionManager(DecisionManager decisionManager) {
        this.decisionManager = decisionManager;
    }
}
