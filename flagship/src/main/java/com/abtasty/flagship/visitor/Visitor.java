package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Flag;
import com.abtasty.flagship.utils.FlagshipContext;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Flagship visitor representation.
 */
public class Visitor implements IVisitor {

    VisitorDelegate delegate;

    /**
     * Specify if how Flagship SDK should handle the newly create visitor instance.
     */
    public enum Instance {

        /**
         * The  newly created visitor instance will be returned and saved into the Flagship singleton. Call `Flagship.getVisitor()` to retrieve the instance.
         * This option should be adopted on applications that handle only one visitor at the same time.
         */
        SINGLE_INSTANCE,

        /**
         * The newly created visitor instance wont be saved and will simply be returned. Any previous visitor instance will have to be recreated.
         * This option should be adopted on applications that handle multiple visitors at the same time.
         */
        NEW_INSTANCE
    }

    /**
     * This class represents a Visitor builder.
     *
     * Use Flagship.visitorBuilder() method to instantiate it.
     */
    public static class Builder {

        private Instance                instanceType = Instance.SINGLE_INSTANCE;
        private final ConfigManager     configManager;
        private final String            visitorId;
        private boolean                 isAuthenticated = false;
        private boolean                 hasConsented = true;
        private HashMap<String, Object> context = null;

        public Builder(Instance instanceType, ConfigManager configManager, String visitorId) {
            this.instanceType = instanceType;
            this.configManager = configManager;
            this.visitorId = visitorId;
        }

        /**
         * Specify if the visitor is authenticated or anonymous.
         *
         * @param isAuthenticated boolean, true for an authenticated visitor, false for an anonymous visitor.
         * @return Builder
         */
        public Builder isAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
            return this;
        }

        /**
         * Specify if the visitor has consented for personal data usage. When false some features will be deactivated, cache will be deactivated and cleared.
         *
         * @param hasConsented @param hasConsented Set to true when the visitor has consented, false otherwise.
         * @return Builder
         */
        public Builder hasConsented(boolean hasConsented) {
            this.hasConsented = hasConsented;
            return this;
        }

        /**
         * Specify visitor initial context key / values used for targeting.
         * Context keys must be String, and values types must be one of the following : Number, Boolean, String.
         *
         * @param context : Initial context.
         * @return Builder
         */
        public Builder context(HashMap<String, Object> context) {
            this.context = context;
            return this;
        }

        /**
         * Create a new visitor.
         *
         * @return Visitor
         */
        public Visitor build() {
            Visitor visitor = new Visitor(configManager, visitorId, isAuthenticated, hasConsented, context);
            if (instanceType == Instance.SINGLE_INSTANCE)
                Flagship.setSingleVisitorInstance(visitor);
            return visitor;
        }
    }

    private Visitor(ConfigManager configManager, String visitorId, Boolean isAuthenticated, Boolean hasConsented, HashMap<String, Object> context) {
        this.delegate = new VisitorDelegate(this, configManager, visitorId, isAuthenticated, hasConsented, context);
    }

    /**
     * Return the current visitor id
     * @return visitor unique identifier
     */

    synchronized public String getId() {
        return delegate.getVisitorId();
    }

    /**
     * Return the current visitor anonymous id.
     * @return visitor anonymous identifier.
     */
    synchronized public String getAnonymousId() {
        return delegate.getAnonymousId();
    }

    @Override
    synchronized public void updateContext(HashMap<String, Object> context) {
        this.delegate.getStrategy().updateContext(context);
    }

    @Override
    synchronized public <T> void updateContext(String key, T value) {
        this.delegate.getStrategy().updateContext(key, value);
    }

    @Override
    synchronized public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        this.delegate.getStrategy().updateContext(flagshipContext, value);
    }

    @Override
    synchronized public void clearContext() {
        this.delegate.getStrategy().clearContext();
    }

    /**
     * This function will call the decision api and update all the campaigns modifications from the server according to the visitor context.
     *
     * @deprecated Use fetchFlags() instead.
     *
     * @return a CompletableFuture for this synchronization
     */
    @Deprecated
    synchronized public CompletableFuture<Visitor> synchronizeModifications() {
        return this.delegate.getStrategy().fetchFlags();
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key, default value will be returned.
     *
     * @deprecated Use getFlag() instead.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @return modification value or default value.
     */
    @Deprecated
    synchronized public <T> T getModification(String key, T defaultValue) {
        return this.delegate.getStrategy().getFlag(key, defaultValue).value(false);
    }

    /**
     * Retrieve a modification value by its key. If no modification match the given key, default value will be returned.
     *
     * @deprecated Use getFlag() instead.
     *
     * @param key          key associated to the modification.
     * @param defaultValue default value to return.
     * @param activate     Set this parameter to true to automatically report on our server that the
     *                     current visitor has seen this modification. It is possible to call activateModification() later.
     * @return modification value or default value.
     */
    @Deprecated
    synchronized public <T> T getModification(String key, T defaultValue, boolean activate) {
        return this.delegate.getStrategy().getFlag(key, defaultValue).value(activate);
    }

    /**
     * Get the campaign modification information value matching the given key.
     *
     * @deprecated Use getFlag("flag key").metadata() instead.
     *
     * @param key key which identify the modification.
     * @return JSONObject containing the modification information.
     */
    @Deprecated
    synchronized public JSONObject getModificationInfo(String key) {
        JSONObject json = this.delegate.getStrategy().getFlag(key, null).metadata().toJSON();
        return (json.length() == 0) ? null : json;
    }

    /**
     * Report this user has seen this modification.
     *
     * @deprecated Use getFlag("flag key").userExposed() instead.
     *
     * @param key key which identify the modification to activate.
     */
    @Deprecated
    synchronized public void activateModification(String key) {
        delegate.getStrategy().getFlag(key, null).userExposed();
    }

    //// new


    @Override
    synchronized public CompletableFuture<Visitor> fetchFlags() {
        return this.delegate.getStrategy().fetchFlags();
    }

    @Override
    synchronized public <T> Flag<T> getFlag(String key, T defaultValue) {
        return this.delegate.getStrategy().getFlag(key, defaultValue);
    }

    ////

    @Override
    synchronized public <T> void sendHit(Hit<T> hit) {
        this.delegate.getStrategy().sendHit(hit);
    }

    @Override
    synchronized public void authenticate(String visitorId) {
        this.delegate.getStrategy().authenticate(visitorId);
    }

    @Override
    synchronized public void unauthenticate() {
        this.delegate.getStrategy().unauthenticate();
    }

    @Override
    synchronized public void setConsent(Boolean hasConsented) {
        this.delegate.getStrategy().setConsent(hasConsented);
    }

    @Override
    synchronized public Boolean hasConsented() {
        return this.delegate.getStrategy().hasConsented();
    }

    /**
     * Get visitor current context key / values.
     *
     * @return return context.
     */
    synchronized public HashMap<String, Object> getContext() {
        return this.delegate.getContextCopy();
    }

        @Override
    synchronized public String toString() {
        return this.delegate.toString();
    }
}