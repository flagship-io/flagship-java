package com.abtasty.flagship.main.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.Flagship;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

abstract class AbstractVisitor {

    private  Boolean    hasConsented = true;

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context keys must be String, and values types must be one of the following : Number, Boolean, String.
     *
     * @param context: HashMap of keys, values.
     */
    public void updateContext(HashMap<String, Object> context) {
        getStrategy().updateContext((Visitor) this, context);
    }

    /**
     * Update the visitor context values, matching the given keys, used for targeting.
     * <p>
     * A new context value associated with this key will be created if there is no previous matching value.
     * Context key must be String, and value type must be one of the following : Number, Boolean, String.
     *
     * @param key:  context key.
     * @param value context value.
     */
    public <T> void updateContext(String key, T value) {
        getStrategy().updateContext((Visitor) this, key, value);
    }

    /**
     * This function will call the decision api and update all the campaigns modifications from the server according to the visitor context.
     *
     * @return a CompletableFuture for this synchronization
     */
    public CompletableFuture<Visitor> synchronizeModifications() {
        return getStrategy().synchronizeModifications((Visitor) this);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key, default value will be returned.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @return modification value or default value.
     */
    public <T> T getModification(String key, T defaultValue) {
        return getStrategy().getModification((Visitor) this, key, defaultValue);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key or if the stored value type and default value type do not match, default value will be returned.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @param activate     Set this parameter to true to automatically report on our server that the
     *                     current visitor has seen this modification. It is possible to call activateModification() later.
     * @return modification value or default value.
     */
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        return getStrategy().getModification((Visitor) this, key, defaultValue, activate);
    }

    /**
     * Get the campaign modification information value matching the given key.
     *
     * @param key key which identify the modification.
     * @return JSONObject containing the modification information.
     */
    public JSONObject getModificationInfo(String key) {
        return getStrategy().getModificationInfo((Visitor) this, key);
    }

    /**
     * Report this user has seen this modification.
     *
     * @param key key which identify the modification to activate.
     */
    public void activateModification(String key) {
        getStrategy().activateModification((Visitor) this, key);
    }

    /**
     * Send a Hit to Flagship servers for reporting.
     *
     * @param hit hit to track.
     */
    public <T> void sendHit(Hit<T> hit) {
        getStrategy().sendHit((Visitor) this, hit);
    }

    protected VisitorStrategy getStrategy() {
        if (Flagship.getStatus().lessThan(Flagship.Status.READY_PANIC_ON))
            return new NotReadyStrategy();
        else if (Flagship.getStatus() == Flagship.Status.READY_PANIC_ON)
            return new PanicStrategy();
        else if (!hasConsented)
            return new NoConsentStrategy();
        else
            return new DefaultStrategy();
    }

    public Boolean hasConsented() {
        return hasConsented;
    }

    public void setConsent(Boolean hasConsented) {
        this.hasConsented = hasConsented;
    }
}
