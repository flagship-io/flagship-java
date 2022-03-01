package com.abtasty.flagship.visitor;

import com.abtasty.flagship.cache.CacheHelper;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.model.Modification;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VisitorDelegateDTO {

    protected   VisitorDelegate                 visitorDelegate;
    protected   ConfigManager                   configManager;
    protected   String                          visitorId;
    protected   String                          anonymousId;
    protected   HashMap<String, Object>         context;
    protected   HashMap<String, Modification>   modifications;
    protected   ArrayList<String>               activatedVariations;
    protected   boolean                         hasConsented;
    protected   boolean                         isAuthenticated;
    protected   HashMap<String, String>         assignmentsHistory;

    public VisitorDelegateDTO(VisitorDelegate visitorDelegate) {

        this.visitorDelegate = visitorDelegate;
        this.configManager = visitorDelegate.getConfigManager();
        this.visitorId = visitorDelegate.getVisitorId();
        this.anonymousId = visitorDelegate.getAnonymousId();
        this.context = new HashMap<>(visitorDelegate.getContext());
        this.modifications = new HashMap<String, Modification>(visitorDelegate.getModifications());
        this.activatedVariations = new ArrayList<>(visitorDelegate.getActivatedVariations());
        this.hasConsented = visitorDelegate.getConsent();
        this.isAuthenticated = visitorDelegate.isAuthenticated();
        this.assignmentsHistory = new HashMap<>(visitorDelegate.getAssignmentsHistory());
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public String toString() {
        return new JSONObject()
                .put("visitorId", visitorId)
                .put("anonymousId", (anonymousId != null) ? anonymousId : JSONObject.NULL)
                .put("isAuthenticated", isAuthenticated)
                .put("hasConsented", hasConsented)
                .put("context", contextToJson())
                .put("modifications", modificationsToJson())
                .put("activatedVariations", new JSONArray(activatedVariations.toArray()))
                .put("assignmentsHistory", new JSONObject(assignmentsHistory))
                .toString(2);
    }

    public JSONObject contextToJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> e : context.entrySet()) {
            json.put(e.getKey(), e.getValue());
        }
        return json;
    }

    public JSONObject modificationsToJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Modification> e : modifications.entrySet()) {
            Object value = e.getValue().getValue();
            json.put(e.getKey(), (value != null) ? value : JSONObject.NULL);
        }
        return json;
    }

    public String getVariationGroupAssignment(String variationGroupId) {
        return assignmentsHistory.getOrDefault(variationGroupId, null);
    }

    public void addNewAssignmentToHistory(String variationGroupId, String variationId) {
//        if (!assignmentsHistory.containsKey(variationGroupId)) //might be not compatible with XPC
        assignmentsHistory.put(variationGroupId, variationId);
        visitorDelegate.getAssignmentsHistory().put(variationGroupId, variationId);
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getAnonymousId() {
        return anonymousId;
    }

    public HashMap<String, Object> getContext() {
        return context;
    }

    public HashMap<String, Modification> getModifications() {
        return modifications;
    }

    public ArrayList<String> getActivatedVariations() {
        return activatedVariations;
    }

    public boolean hasConsented() {
        return hasConsented;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public VisitorDelegate getVisitorDelegate() {
        return visitorDelegate;
    }

    public HashMap<String, String> getAssignmentsHistory() {
        return assignmentsHistory;
    }
}
