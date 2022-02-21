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
    public       VisitorCache                            cachedVisitor;

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
        cachedVisitor = new VisitorCache(this);
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

}