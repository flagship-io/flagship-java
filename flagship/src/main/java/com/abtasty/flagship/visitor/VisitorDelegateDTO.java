package com.abtasty.flagship.visitor;

import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.model.Modification;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VisitorDelegateDTO {

    private VisitorDelegate                 visitorDelegate;
    private ConfigManager                   configManager;
    private String                          visitorId;
    private String                          anonymousId;
    private HashMap<String, Object>         context;
    private HashMap<String, Modification>   modifications;
    private ArrayList<String>               activatedVariations;
    private boolean                         hasConsented;
    private boolean                         isAuthenticated;

    public VisitorDelegateDTO(VisitorDelegate visitorDelegate) {

        this.visitorDelegate = visitorDelegate;
        this.configManager = visitorDelegate.configManager;
        this.visitorId = visitorDelegate.visitorId;
        this.anonymousId = visitorDelegate.anonymousId;
        this.context = visitorDelegate.getContext();
        this.modifications = new HashMap<String, Modification>(visitorDelegate.modifications);
        this.activatedVariations = new ArrayList<>(visitorDelegate.activatedVariations);
        this.hasConsented = visitorDelegate.hasConsented;
        this.isAuthenticated = visitorDelegate.isAuthenticated;
    }

    public JSONObject getContextAsJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> e : context.entrySet()) {
            json.put(e.getKey(), e.getValue());
        }
        return json;
    }

    public boolean isVariationAssigned(String variationId) {
        for (Map.Entry<String, Modification> e : modifications.entrySet()) {
            if (Objects.equals(e.getValue().getVariationId(), variationId))
                return true;
        }
        return false;
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
                .put("context", getContextAsJson())
                .put("modifications", getModificationsAsJson())
                .put("activatedVariations", new JSONArray(activatedVariations.toArray())).toString(2);
    }

    public JSONObject getModificationsAsJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Modification> e : modifications.entrySet()) {
            Object value = e.getValue().getValue();
            json.put(e.getKey(), (value != null) ? value : JSONObject.NULL);
        }
        return json;
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
}
