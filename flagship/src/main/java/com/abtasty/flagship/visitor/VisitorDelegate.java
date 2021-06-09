package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipContext;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * Delegate for Visitor
 */
public class VisitorDelegate extends AbstractVisitor implements IVisitor {

    private final Visitor visitor;
    /**
     * Create a new visitor delegate.
     *
     * @param visitor  visitor to delegate.
     */
    public VisitorDelegate(Visitor visitor) {
       this.visitor = visitor;
    }

    private VisitorStrategy getStrategy() {
        if (Flagship.getStatus().lessThan(Flagship.Status.PANIC))
            return new NotReadyStrategy(this);
        else if (Flagship.getStatus() == Flagship.Status.PANIC)
            return new PanicStrategy(this);
        else if (!visitor.hasConsented())
            return new NoConsentStrategy(this);
        else
            return new DefaultStrategy(this);
    }

    /*
     *  Visitor public methods
     */

    @Override
    public void updateContext(HashMap<String, Object> context) {
        getStrategy().updateContext(context);
    }

    @Override
    public <T> void updateContext(String key, T value) {
        getStrategy().updateContext(key, value);
    }

    @Override
    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        getStrategy().updateContext(flagshipContext, value);
    }

    @Override
    public void clearContext() {
        getStrategy().clearContext();
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications() {
        return getStrategy().synchronizeModifications();
    }

    @Override
    public <T> T getModification(String key, T defaultValue) {
        return getStrategy().getModification(key, defaultValue);
    }

    @Override
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        return getStrategy().getModification(key, defaultValue, activate);
    }

    @Override
    public JSONObject getModificationInfo(String key) {
        return getStrategy().getModificationInfo(key);
    }

    @Override
    public void activateModification(String key) {
        getStrategy().activateModification(key);
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        getStrategy().sendHit(hit);
    }

    @Override
    public void authenticate(String visitorId) {
        getStrategy().authenticate(visitorId);
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void unauthenticate() {
        getStrategy().unauthenticate();
    }

    /*
     *  Delegate methods
     */

    public Visitor getOriginalVisitor() {
        return visitor;
    }

    public ConcurrentMap<String, Modification> getVisitorModifications() {
        return visitor.modifications;
    }

    public ConcurrentMap<String, Object> getVisitorContext() {
        return visitor.context;
    }

    public void updateModifications(HashMap<String, Modification> modifications) {
        if (modifications != null) {
            visitor.modifications.clear();
            visitor.modifications.putAll(modifications);
        }
    }

    public void sendContextRequest() {
        getStrategy().sendContextRequest();
    }

    /*
     *  Visitor abstract methods.
     */

    @Override
    public String getId() {
        return visitor.getId();
    }

    @Override
    public void setId(String id) {
       visitor.setId(id);
    }

    @Override
    public String getAnonymousId() {
        return visitor.getAnonymousId();
    }

    @Override
    public void setAnonymousId(String anonymousId) {
        visitor.setAnonymousId(anonymousId);
    }

    @Override
    public HashMap<String, Object> getContext() {
        return visitor.getContext();
    }

    @Override
    public Boolean hasConsented() {
        return visitor.hasConsented();
    }

    @Override
    public void setConsent(Boolean hasConsented) {
        visitor.setConsent(hasConsented);
    }

    @Override
    public void logVisitor(FlagshipLogManager.Tag tag) {
        visitor.logVisitor(tag);
    }

    @Override
    public ConfigManager getConfigManager() {
        return visitor.getConfigManager();
    }

    @Override
    public JSONObject getContextAsJson() {
        return visitor.getContextAsJson();
    }

    @Override
    public JSONObject getModificationsAsJson() {
        return visitor.getContextAsJson();
    }

    @Override
    protected void loadContext(HashMap<String, Object> newContext) {
        getStrategy().loadContext(newContext);
    }
}
