package com.abtasty.flagship.visitor;

import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Delegate for Visitor
 */
public class VisitorDelegate {

//    public final ConfigManager                           configManager;
//    public       String                                  visitorId;
//    public       String                                  anonymousId;
//    public       ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
//    public       ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();
//    public       ConcurrentLinkedQueue<String>           activatedVariations = new ConcurrentLinkedQueue<>();
//    public       Boolean                                 hasConsented;
//    public       Boolean                                 isAuthenticated;
//    public       Visitor                                 originalVisitor;
    private final Visitor                                 originalVisitor;
    private final ConfigManager                           configManager;
    private       String                                  visitorId;
    private       String                                  anonymousId;
    private final ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String>           activatedVariations = new ConcurrentLinkedQueue<>();
    private       Boolean                                 hasConsented;
    private       Boolean                                 isAuthenticated;
    private final ConcurrentHashMap<String, String>       assignmentsHistory = new ConcurrentHashMap<>();
//    public       VisitorCache                            cachedVisitor;

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
//        cachedVisitor = new VisitorCache(this);
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

    HashMap<String, Object> getContextCopy() {
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

    synchronized public Visitor getOriginalVisitor() {
        return originalVisitor;
    }

//    public void setOriginalVisitor(Visitor originalVisitor) {
//        this.originalVisitor = originalVisitor;
//    }

    synchronized public ConfigManager getConfigManager() {
        return configManager;
    }

    synchronized public String getVisitorId() {
        return visitorId;
    }

    synchronized public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    synchronized public String getAnonymousId() {
        return anonymousId;
    }

    synchronized public void setAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
    }

//    public void setContext(ConcurrentMap<String, Object> context) {
//        this.context = context;
//    }

    synchronized public ConcurrentMap<String, Modification> getModifications() {
        return modifications;
    }

//    public void setModifications(ConcurrentMap<String, Modification> modifications) {
//        this.modifications = modifications;
//    }

    synchronized public ConcurrentLinkedQueue<String> getActivatedVariations() {
        return activatedVariations;
    }

//    public void setActivatedVariations(ConcurrentLinkedQueue<String> activatedVariations) {
//        this.activatedVariations = activatedVariations;
//    }

    synchronized public Boolean getConsent() {
        return hasConsented;
    }

    synchronized public void setConsent(Boolean hasConsented) {
        this.hasConsented = hasConsented;
    }

    synchronized public Boolean isAuthenticated() {
        return isAuthenticated;
    }

    synchronized public void isAuthenticated(Boolean authenticated) {
        isAuthenticated = authenticated;
    }

    synchronized public ConcurrentHashMap<String, String> getAssignmentsHistory() {
        return assignmentsHistory;
    }

    synchronized public ConcurrentMap<String, Object> getContext() {
        return context;
    }


//    public void setAssignmentsHistory(ConcurrentHashMap<String, String> assignmentsHistory) {
//        this.assignmentsHistory = assignmentsHistory;
//    }
}