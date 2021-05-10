package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Flagship visitor representation.
 */


public class Visitor extends AbstractVisitor {

    private   final ConfigManager                           configManager;
    protected final String                                  visitorId;
    protected final ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();

    /**
     * Create a new visitor.
     *
     * @param managers      configured managers.
     * @param visitorId     visitor unique identifier.
     * @param context       visitor context.
     */
    public Visitor(ConfigManager managers, String visitorId, HashMap<String, Object> context) {
        this.configManager = managers;
        this.visitorId = (visitorId == null || visitorId.length() <= 0) ? genVisitorId() : visitorId;
        this.updateContext(context);
    }

    private String genVisitorId() {
        FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, FlagshipConstants.Warnings.VISITOR_ID_NULL_OR_EMPTY);
        return UUID.randomUUID().toString();
    }

    /**
     * Get visitor current context key / values.
     * @return return context.
     */
    public ConcurrentMap<String, Object> getContext() {
        return this.context;
    }

    void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, this);
        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
    }

    protected ConfigManager getManagerConfig() {
        return configManager;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("visitorId", visitorId);
        json.put("context", getContextAsJson());
        json.put("modifications", getModificationsAsJson());
        return json.toString(2);
    }

    public String getId() {
        return visitorId;
    }

    public JSONObject getContextAsJson() {
        JSONObject contextJson = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            contextJson.put(e.getKey(), e.getValue());
        }
        return contextJson;
    }

    private JSONObject getModificationsAsJson() {
        JSONObject modificationJson = new JSONObject();
        this.modifications.forEach((flag, modification) -> {
            Object value = modification.getValue();
            modificationJson.put(flag, (value == null) ? JSONObject.NULL : value);
        });
        return modificationJson;
    }

    @Override
    public void clearVisitorData() {
        //clear all visitor data.
    }
}
