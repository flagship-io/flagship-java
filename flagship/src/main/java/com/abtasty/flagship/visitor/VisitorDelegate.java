package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

///**
// * Delegate for Visitor
// */
//public class VisitorDelegate extends AbstractVisitor implements IVisitor {
//
//    private final Visitor visitor;
//    /**
//     * Create a new visitor delegate.
//     *
//     * @param visitor  visitor to delegate.
//     */
//    public VisitorDelegate(Visitor visitor) {
//       this.visitor = visitor;
//    }
//
//    private VisitorStrategy getStrategy() {
//        if (Flagship.getStatus().lessThan(Flagship.Status.PANIC))
//            return new NotReadyStrategy(this);
//        else if (Flagship.getStatus() == Flagship.Status.PANIC)
//            return new PanicStrategy(this);
//        else if (!visitor.hasConsented())
//            return new NoConsentStrategy(this);
//        else
//            return new DefaultStrategy(this);
//    }
//
//    /*
//     *  Visitor public methods
//     */
//
//    @Override
//    public void updateContext(HashMap<String, Object> context) {
//        getStrategy().updateContext(context);
//    }
//
//    @Override
//    public <T> void updateContext(String key, T value) {
//        getStrategy().updateContext(key, value);
//    }
//
//    @Override
//    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
//        getStrategy().updateContext(flagshipContext, value);
//    }
//
//    @Override
//    public void clearContext() {
//        getStrategy().clearContext();
//    }
//
//    @Override
//    public CompletableFuture<Visitor> synchronizeModifications() {
//        return getStrategy().synchronizeModifications();
//    }
//
//    @Override
//    public <T> T getModification(String key, T defaultValue) {
//        return getStrategy().getModification(key, defaultValue);
//    }
//
//    @Override
//    public <T> T getModification(String key, T defaultValue, boolean activate) {
//        return getStrategy().getModification(key, defaultValue, activate);
//    }
//
//    @Override
//    public JSONObject getModificationInfo(String key) {
//        return getStrategy().getModificationInfo(key);
//    }
//
//    @Override
//    public void activateModification(String key) {
//        getStrategy().activateModification(key);
//    }
//
//    @Override
//    public <T> void sendHit(Hit<T> hit) {
//        getStrategy().sendHit(hit);
//    }
//
//    @Override
//    public void authenticate(String visitorId) {
//        getStrategy().authenticate(visitorId);
//    }
//
//    @Override
//    @SuppressWarnings("SpellCheckingInspection")
//    public void unauthenticate() {
//        getStrategy().unauthenticate();
//    }
//
//    /*
//     *  Delegate methods
//     */
//
//    public Visitor getOriginalVisitor() {
//        return visitor;
//    }
//
//    public ConcurrentMap<String, Modification> getVisitorModifications() {
//        return visitor.modifications;
//    }
//
//    public ConcurrentMap<String, Object> getVisitorContext() {
//        return visitor.context;
//    }
//
//    public void updateModifications(HashMap<String, Modification> modifications) {
//        if (modifications != null) {
//            visitor.modifications.clear();
//            visitor.modifications.putAll(modifications);
//        }
//    }
//
//    public void sendContextRequest() {
//        getStrategy().sendContextRequest();
//    }
//
//    public void sendConsentRequest() {
//        getStrategy().sendConsentRequest();
//    }
//
//    /*
//     *  Visitor abstract methods.
//     */
//
//    @Override
//    public String getId() {
//        return visitor.getId();
//    }
//
//    @Override
//    public void setId(String id) {
//       visitor.setId(id);
//    }
//
//    @Override
//    public String getAnonymousId() {
//        return visitor.getAnonymousId();
//    }
//
//    @Override
//    public void setAnonymousId(String anonymousId) {
//        visitor.setAnonymousId(anonymousId);
//    }
//
//    @Override
//    public HashMap<String, Object> getContext() {
//        return visitor.getContext();
//    }
//
//    @Override
//    public Boolean hasConsented() {
//        return visitor.hasConsented();
//    }
//
//    @Override
//    public void setConsent(Boolean hasConsented) {
//        getStrategy().sendConsentRequest();
//    }
//
//    @Override
//    public void logVisitor(FlagshipLogManager.Tag tag) {
//        visitor.logVisitor(tag);
//    }
//
//    @Override
//    public ConfigManager getConfigManager() {
//        return visitor.getConfigManager();
//    }
//
//    @Override
//    public JSONObject getContextAsJson() {
//        return visitor.getContextAsJson();
//    }
//
//    @Override
//    public JSONObject getModificationsAsJson() {
//        return visitor.getContextAsJson();
//    }
//
//    @Override
//    protected void loadContext(HashMap<String, Object> newContext) {
//        getStrategy().loadContext(newContext);
//    }
//}

public class VisitorDelegate {

    private   final ConfigManager                           configManager;
    protected       String                                  visitorId;
    protected       String                                  anonymousId;
    protected       ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
    protected       ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();
    protected       Boolean                                 hasConsented;
    protected       Boolean                                 isAuthenticated;
    private         Visitor                                 visitor;

    public VisitorDelegate(ConfigManager configManager, String visitorId, Boolean isAuthenticated, Boolean hasConsented, HashMap<String, Object> context) {
        this.configManager = configManager;
        this.visitorId = (visitorId == null || visitorId.length() <= 0) ? genVisitorId() : visitorId;
        this.isAuthenticated = isAuthenticated;
        this.hasConsented = hasConsented;
        this.loadContext(context);
        if (this.configManager.getFlagshipConfig().getDecisionMode() == Flagship.DecisionMode.API && isAuthenticated)
            this.anonymousId = genVisitorId();
        else
            this.anonymousId = null;
        if (!this.hasConsented)
            getStrategy().sendConsentRequest();
    }

    protected VisitorStrategy getStrategy() {
        if (Flagship.getStatus().lessThan(Flagship.Status.PANIC))
            return new NotReadyStrategy(this);
        else if (Flagship.getStatus() == Flagship.Status.PANIC)
            return new PanicStrategy(this);
        else if (!hasConsented)
            return new NoConsentStrategy(this);
        else
            return new DefaultStrategy(this);
    }

    public JSONObject getContextAsJson() {
        JSONObject contextJson = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            contextJson.put(e.getKey(), (e.getValue() != null) ? e.getValue() : JSONObject.NULL);
        }
        return contextJson;
    }

    public JSONObject getModificationsAsJson() {
        JSONObject modificationJson = new JSONObject();
        this.modifications.forEach((flag, modification) -> {
            Object value = modification.getValue();
            modificationJson.put(flag, (value == null) ? JSONObject.NULL : value);
        });
        return modificationJson;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("visitorId", visitorId);
        json.put("anonymousId", (anonymousId != null) ? anonymousId : JSONObject.NULL);
        json.put("context", getContextAsJson());
        json.put("modifications", getModificationsAsJson());
        return json.toString(2);
    }

    protected void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, this);
        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getAnonymousId() {
        return anonymousId;
    }

    public ConcurrentMap<String, Object> getVisitorContext() {
        return context;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConcurrentMap<String, Modification> getModifications() {
        return modifications;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public void setAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    public void loadContext(HashMap<String, Object> newContext) {
        getStrategy().loadContext(newContext);
    }

    public HashMap<String, Object> getContext() {
        return new HashMap<>(context);
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

    public Boolean hasConsented() {
        return hasConsented;
    }

    public void sendContextRequest() {
        getStrategy().sendContextRequest();
    }

    public void updateModifications(HashMap<String, Modification> modifications) {
        if (modifications != null) {
            this.modifications.clear();
            this.modifications.putAll(modifications);
        }
    }

    public Visitor getVisitor() {
        return this.visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }
}