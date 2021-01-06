package com.abtasty.flagship.main;

import java.util.HashMap;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.*;

public class Visitor {

    private String visitorId = null;
    private FlagshipConfig config = null;
    private HashMap<String, Object> context = new HashMap<>();

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
        } else
            config.logManager.onLog(LogManager.Tag.VISITOR_CONTEXT, LogLevel.WARNING, FlagshipConstants.UPDATE_CONTEXT + FlagshipConstants.CONTEXT_PARAM_ERROR);
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "visitorId='" + visitorId + '\'' +
                ", config=" + config +
                ", context=" + context +
                '}';
    }
}
