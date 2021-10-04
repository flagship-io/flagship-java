package com.abtasty.flagship.visitor;

import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.ConfigManager;
import com.abtasty.flagship.main.Flagship;
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
        this.delegate = new VisitorDelegate(configManager, visitorId, isAuthenticated, hasConsented, context);
        this.delegate.setVisitor(this);
    }

    @Override
    public void updateContext(HashMap<String, Object> context) {
        this.delegate.getStrategy().updateContext(context);
    }

    @Override
    public <T> void updateContext(String key, T value) {
        this.delegate.getStrategy().updateContext(key, value);
    }

    @Override
    public <T> void updateContext(FlagshipContext<T> flagshipContext, T value) {
        this.delegate.getStrategy().updateContext(flagshipContext, value);
    }

    @Override
    public void clearContext() {
        this.delegate.getStrategy().clearContext();
    }

    @Override
    public CompletableFuture<Visitor> synchronizeModifications() {
        return this.delegate.getStrategy().synchronizeModifications();
    }

    @Override
    public <T> T getModification(String key, T defaultValue) {
        return this.delegate.getStrategy().getModification(key, defaultValue);
    }

    @Override
    public <T> T getModification(String key, T defaultValue, boolean activate) {
        return this.delegate.getStrategy().getModification(key, defaultValue, activate);
    }

    @Override
    public JSONObject getModificationInfo(String key) {
        return this.delegate.getStrategy().getModificationInfo(key);
    }

    @Override
    public void activateModification(String key) {
        this.delegate.getStrategy().activateModification(key);
    }

    @Override
    public <T> void sendHit(Hit<T> hit) {
        this.delegate.getStrategy().sendHit(hit);
    }

    @Override
    public void authenticate(String visitorId) {
        this.delegate.getStrategy().authenticate(visitorId);
    }

    @Override
    public void unauthenticate() {
        this.delegate.getStrategy().unauthenticate();
    }

    @Override
    public void setConsent(Boolean hasConsented) {
        this.delegate.getStrategy().setConsent(hasConsented);
    }

    @Override
    public Boolean hasConsented() {
        return this.delegate.getStrategy().hasConsented();
    }

    /**
     * Get visitor current context key / values.
     *
     * @return return context.
     */
    public HashMap<String, Object> getContext() {
        return this.delegate.getContext();
    }

        @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("visitorId", this.delegate.visitorId);
        json.put("anonymousId", (this.delegate.anonymousId != null) ? this.delegate.anonymousId : JSONObject.NULL);
        json.put("context", this.delegate.getContextAsJson());
        json.put("modifications", this.delegate.getModificationsAsJson());
        return json.toString(2);
    }
}