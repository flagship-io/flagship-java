package com.abtasty.flagship.visitor;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Flagship visitor representation.
 */
public class Visitor implements IVisitor {

    protected final String                                  visitorId;
    protected final ConcurrentMap<String, Object>           context = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, Modification>     modifications = new ConcurrentHashMap<>();
    protected final DecisionManager                         decisionManager;
    protected final TrackingManager                         trackingManager;
    private         VisitorStrategy                         currentStrategy = new VisitorStrategy();
    private         Boolean                                 hasConsented = true;

    /**
     * Create a new visitor.
     *
     * @param config    configuration used when the visitor has been created.
     * @param visitorId visitor unique identifier.
     * @param context   visitor context.
     */
    public Visitor(FlagshipConfig config, DecisionManager decisionManager, String visitorId, HashMap<String, Object> context) {
        this.decisionManager = decisionManager;
        this.trackingManager = config.getTrackingManager();
        this.visitorId = visitorId;
        this.updateContext(context);
    }

    private VisitorStrategy getStrategy() {
        if (Flagship.getStatus().lessThan(Flagship.Status.READY_PANIC_ON) && !(currentStrategy instanceof NotReadyStrategy))
            currentStrategy = new NotReadyStrategy();
        else if (Flagship.getStatus() == Flagship.Status.READY_PANIC_ON && !(currentStrategy instanceof PanicStrategy))
            currentStrategy = new PanicStrategy();
        else if (!hasConsented && !(currentStrategy instanceof NoConsentStrategy))
            currentStrategy = new NoConsentStrategy();
        else if (Flagship.getStatus() == Flagship.Status.READY &&
                        ((currentStrategy instanceof PanicStrategy) ||
                        (currentStrategy instanceof NoConsentStrategy) ||
                        (currentStrategy instanceof NotReadyStrategy)))
            currentStrategy = new VisitorStrategy();
        return currentStrategy;
    }

    /**
     * Get visitor current context key / values.
     * @return return context.
     */
    public ConcurrentMap<String, Object> getContext() {
        return this.context;
    }

    @Override
    public void updateContext(HashMap<String, Object> context) {
        getStrategy().updateContext(this, context);
    }

    @Override
    public <T> void updateContext(String key, T value) {
        getStrategy().updateContext(this, key, value);
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications() {
       return getStrategy().synchronizeModifications(this);
    }

    @Override
    public <T> T getModification(String key, T defaultValue) {
        return getStrategy().getModification(this, key, defaultValue);
    }

    @Override
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        return getStrategy().getModification(this, key, defaultValue, activate);
    }

    @Override
    public JSONObject getModificationInfo(String key) {
        return getStrategy().getModificationInfo(this, key);
    }

    @Override
    public void activateModification(String key) {
        getStrategy().activateModification(this, key);
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        getStrategy().sendHit(this, hit);
    }

    protected void logVisitor(FlagshipLogManager.Tag tag) {
        String visitorStr = String.format(FlagshipConstants.Errors.VISITOR, visitorId, this);
        FlagshipLogManager.log(tag, LogManager.Level.DEBUG, visitorStr);
    }

    public Boolean hasConsented() {
        return hasConsented;
    }

    public void setConsent(Boolean consent) {
        this.hasConsented = consent;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("visitorId", visitorId);
        JSONObject contextJson = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            contextJson.put(e.getKey(), e.getValue());
        }
        JSONObject modificationJson = new JSONObject();
        this.modifications.forEach((flag, modification) -> {
            Object value = modification.getValue();
            modificationJson.put(flag, (value == null) ? JSONObject.NULL : value);
        });
        json.put("context", contextJson);
        json.put("modifications", modificationJson);
        return json.toString(2);
    }
}
