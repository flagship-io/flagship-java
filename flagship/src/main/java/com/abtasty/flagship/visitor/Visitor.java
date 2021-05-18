package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Flagship visitor representation.
 */
public class Visitor extends AbstractVisitor implements IVisitor {

    private   final ConfigManager                           configManager;
    protected final String                                  visitorId;
    protected       ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
    protected       ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();
    protected       Boolean                                 hasConsented = true;

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
        this.loadContext();
        this.updateContext(context);
    }

    /*
     *   Visitor public methods
     */

    @Override
    public void updateContext(HashMap<String, Object> context) {
        new VisitorDelegate(this).updateContext(context);
        logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public <T> void updateContext(String key, T value) {
        new VisitorDelegate(this).updateContext(key, value);
        logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        new VisitorDelegate(this).updateContext(flagshipContext, value);
        logVisitor(FlagshipLogManager.Tag.UPDATE_CONTEXT);
    }

    @Override
    public void clearContext() {
        new VisitorDelegate(this).clearContext();
        logVisitor(FlagshipLogManager.Tag.CLEAR_CONTEXT);
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications() {
        return new VisitorDelegate(this).synchronizeModifications();
    }

    @Override
    public <T> T getModification(String key, T defaultValue) {
        return new VisitorDelegate(this).getModification(key, defaultValue);
    }

    @Override
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        return new VisitorDelegate(this).getModification(key, defaultValue, activate);
    }

    @Override
    public JSONObject getModificationInfo(String key) {
        return new VisitorDelegate(this).getModificationInfo(key);
    }

    @Override
    public void activateModification(String key) {
        new VisitorDelegate(this).activateModification(key);
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        new VisitorDelegate(this).sendHit(hit);
    }

    /*
     *   Visitor private methods
     */

    /**
     * Generated a visitor id in a form of UUID
     * @return a unique identifier
     */
    private String genVisitorId() {
        FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, FlagshipConstants.Warnings.VISITOR_ID_NULL_OR_EMPTY);
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void loadContext() {
        if (FlagshipContext.autoLoading) {
            VisitorDelegate delegate = new VisitorDelegate(this);
            for (FlagshipContext flagshipContext : FlagshipContext.ALL) {
                delegate.updateContext(flagshipContext, flagshipContext.load(delegate));
            }
        }
    }

    /*
     *   Visitor abstract methods
     */

    /**
     * Return the visitor unique identifier.
     * @return visitor unique identifier.
     */
    @Override
    public String getId() {
        return visitorId;
    }

    @Override
    protected ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get visitor current context key / values.
     * @return return context.
     */
    @Override
    public HashMap<String, Object> getContext() {
        HashMap<String, Object> map = new HashMap<>();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    /**
     * Return if the visitor has given his consent for private data usage.
     * @return return a true if the visitor has given consent, false otherwise.
     */
    @Override
    public Boolean hasConsented() {
        return hasConsented;
    }

    /**
     * Set visitor consent for private data usage. When false some features will be deactivated, cache will be deactivated and cleared.
     * @param hasConsented Set to true when the visitor has consented, false otherwise.
     */
    @Override
    public void setConsent(Boolean hasConsented) {
        this.hasConsented = hasConsented;
        if (!hasConsented)
            clearVisitorData();
    }

    @Override
    protected void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, this);
        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("visitorId", visitorId);
        json.put("context", getContextAsJson());
        json.put("modifications", getModificationsAsJson());
        return json.toString(2);
    }

    @Override
    protected JSONObject getContextAsJson() {
        JSONObject contextJson = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            contextJson.put(e.getKey(), e.getValue());
        }
        return contextJson;
    }

    @Override
    protected JSONObject getModificationsAsJson() {
        JSONObject modificationJson = new JSONObject();
        this.modifications.forEach((flag, modification) -> {
            Object value = modification.getValue();
            modificationJson.put(flag, (value == null) ? JSONObject.NULL : value);
        });
        return modificationJson;
    }

    @Override
    protected   void clearVisitorData() {
    }
}
