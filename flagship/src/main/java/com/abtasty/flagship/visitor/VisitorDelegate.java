package com.abtasty.flagship.visitor;

import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Delegate for Visitor
 */
public class VisitorDelegate {

    public final ConfigManager                           configManager;
    public       String                                  visitorId;
    public       String                                  anonymousId;
    public       ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
    public       ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();
    public       ConcurrentLinkedQueue<String>           activatedVariations = new ConcurrentLinkedQueue<>();
    public       Boolean                                 hasConsented;
    public       Boolean                                 isAuthenticated;
    public       Visitor                                 originalVisitor;

    public VisitorDelegate(Visitor originalVisitor, ConfigManager configManager, String visitorId, Boolean isAuthenticated, Boolean hasConsented, HashMap<String, Object> context) {
        this.originalVisitor = originalVisitor;
        this.configManager = configManager;
        this.visitorId = (visitorId == null || visitorId.length() <= 0) ? genVisitorId() : visitorId;
        this.isAuthenticated = isAuthenticated;
        this.hasConsented = hasConsented;
        if (this.configManager.getFlagshipConfig().getDecisionMode() == Flagship.DecisionMode.API && isAuthenticated)
            this.anonymousId = genVisitorId();
        else
            this.anonymousId = null;
        getStrategy().lookupVisitorCache();
        getStrategy().lookupHitCache();
        this.loadContext(context);
        getStrategy().sendConsentRequest();
        logVisitor(FlagshipLogManager.Tag.VISITOR);
    }

    public VisitorStrategy getStrategy() {
        if (Flagship.getStatus().lessThan(Flagship.Status.PANIC))
            return new NotReadyStrategy(this);
        else if (Flagship.getStatus() == Flagship.Status.PANIC)
            return new PanicStrategy(this);
        else if (!hasConsented)
            return new NoConsentStrategy(this);
        else
            return new DefaultStrategy(this);
    }

    /**
     * Generated a visitor id in a form of UUID
     *
     * @return a unique identifier
     */
    private String genVisitorId() {
        FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, FlagshipConstants.Warnings.VISITOR_ID_NULL_OR_EMPTY);
        return UUID.randomUUID().toString();
    }

    HashMap<String, Object> getContext() {
        return new HashMap<>(context);
    }

    void loadContext(HashMap<String, Object> newContext) {
        getStrategy().loadContext(newContext);
    }

    protected void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, this);
        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
    }

    public void updateModifications(HashMap<String, Modification> modifications) {
        if (modifications != null) {
            this.modifications.clear();
            this.modifications.putAll(modifications);
        }
    }

    protected VisitorDelegateDTO toDTO() {
        return new VisitorDelegateDTO(this);
    }

    public String toString() {
        return toDTO().toString();
    }


    //    public JSONObject getContextAsJson() {
//        JSONObject contextJson = new JSONObject();
//        for (HashMap.Entry<String, Object> e : context.entrySet()) {
//            contextJson.put(e.getKey(), (e.getValue() != null) ? e.getValue() : JSONObject.NULL);
//        }
//        return contextJson;
//    }
//
//    public JSONObject getModificationsAsJson() {
//        JSONObject modificationJson = new JSONObject();
//        this.modifications.forEach((flag, modification) -> {
//            Object value = modification.getValue();
//            modificationJson.put(flag, (value == null) ? JSONObject.NULL : value);
//        });
//        return modificationJson;
//    }
//
//    @Override
//    public String toString() {
//        JSONObject json = new JSONObject();
//        json.put("visitorId", visitorId);
//        json.put("anonymousId", (anonymousId != null) ? anonymousId : JSONObject.NULL);
//        json.put("context", getContextAsJson());
//        json.put("modifications", getModificationsAsJson());
//        return json.toString(2);
//    }
//
//    protected void logVisitor(FlagshipLogManager.Tag tag) {
//        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, this);
//        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
//    }
//
//    public String getVisitorId() {
//        return visitorId;
//    }
//
//    public String getAnonymousId() {
//        return anonymousId;
//    }
//
//    public ConcurrentMap<String, Object> getVisitorContext() {
//        return context;
//    }
//
//    public ConfigManager getConfigManager() {
//        return configManager;
//    }
//
//    public ConcurrentMap<String, Modification> getModifications() {
//        return modifications;
//    }
//
//    public void setVisitorId(String visitorId) {
//        this.visitorId = visitorId;
//    }
//
//    public void setAnonymousId(String anonymousId) {
//        this.anonymousId = anonymousId;
//    }
//
//    public void loadContext(HashMap<String, Object> newContext) {
//        getStrategy().loadContext(newContext);
//    }
//
//    public HashMap<String, Object> getContext() {
//        return new HashMap<>(context);
//    }
//
//    /**
//     * Generated a visitor id in a form of UUID
//     *
//     * @return a unique identifier
//     */
//    private String genVisitorId() {
//        FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, FlagshipConstants.Warnings.VISITOR_ID_NULL_OR_EMPTY);
//        return UUID.randomUUID().toString();
//    }
//
//    public Boolean hasConsented() {
//        return hasConsented;
//    }
//
//    public void sendContextRequest() {
//        getStrategy().sendContextRequest();
//    }
//
//    public void updateModifications(HashMap<String, Modification> modifications) {
//        if (modifications != null) {
//            this.modifications.clear();
//            this.modifications.putAll(modifications);
//        }
//    }
//
//    public ConcurrentLinkedQueue<String> getActivatedVariations() {
//        return activatedVariations;
//    }
//
//    public void setActivatedVariations(ConcurrentLinkedQueue<String> activatedVariations) {
//        this.activatedVariations = activatedVariations;
//    }
}