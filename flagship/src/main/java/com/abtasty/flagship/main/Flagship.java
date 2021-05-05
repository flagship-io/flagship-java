package com.abtasty.flagship.main;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.BucketingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import java.util.HashMap;
import java.util.UUID;

/**
 * Flagship main singleton.
 */
public class Flagship {

    private static volatile Flagship    instance = null;

    private FlagshipConfig              config = FlagshipConfig.emptyConfig();
    private DecisionManager             decisionManager = null;
    private Status                      status = Status.NOT_READY;

    public enum Mode {
        DECISION_API,
        BUCKETING,
    }

    public enum Status {
        /**
         * Flagship SDK has not been started or initialized successfully.
         */
        NOT_READY,
        /**
         * Flagship SDK is running in Panic mode.
         */
        READY_PANIC_ON,
        /**
         * Flagship SDK is ready to use.
         */
        READY;
    }

    public interface OnStatusChangedListener {
        void onStatusChanged(Status newStatus);
    }

    protected static Flagship instance() {
        if (instance == null) {
            synchronized (Flagship.class) {
                if (instance == null)
                    instance = new Flagship();
            }
        }
        return instance;
    }

    /**
     * Start the flagship SDK, with the default configuration.
     *
     * @param envId  : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     */
    public static void start(String envId, String apiKey) {
        start(envId, apiKey, null);
    }

    /**
     * Start the flagship SDK, with a custom configuration implementation.
     *
     * @param envId  : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     * @param config : SDK configuration. @see FlagshipConfig
     */
    public static void start(String envId, String apiKey, FlagshipConfig config) {
        instance().setStatus(Status.NOT_READY);
        if (envId != null && apiKey != null) {
            if (config == null)
                config = new FlagshipConfig(envId, apiKey);
            config.withEnvId(envId);
            config.withApiKey(apiKey);
            instance().setConfig(config);
            DecisionManager decisionManager = (config.getDecisionMode() == Flagship.Mode.DECISION_API) ? new ApiManager(config) : new BucketingManager(config);
            decisionManager.setOnStatusChangedListener(newStatus -> instance().setStatus(newStatus));
            instance().setDecisionManager(decisionManager);
        } else
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, LogManager.Level.ERROR, FlagshipConstants.Errors.INITIALIZATION_PARAM_ERROR);
    }

    public static Status getStatus() {
        return instance().status;
    }

    protected void setStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            if (this.config != null && this.config.getOnStatusChangedListener() != null)
                config.getOnStatusChangedListener().onStatusChanged(status);
            if (this.status == Status.READY)
                FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, LogManager.Level.INFO, String.format(FlagshipConstants.Info.STARTED, BuildConfig.flagship_version_name));
        }
    }

    /**
     * Return the current used configuration.
     *
     * @return FlagshipConfig
     */
    public static FlagshipConfig getConfig() {
        return instance().config;
    }

    protected void setConfig(FlagshipConfig config) {
        if (config != null) {
            this.config = config;
        }
    }

    protected void setDecisionManager(DecisionManager decisionManager) {
        this.decisionManager = decisionManager;
    }

    protected static DecisionManager getDecisionManager() {
        return instance().decisionManager;
    }

    /**
     * Create a new visitor without context.
     *
     * @param visitorId : Unique visitor identifier.
     * @return Visitor
     */
    public static Visitor newVisitor(String visitorId) {
        return newVisitor(visitorId, null);
    }

    /**
     * Create a new visitor with a context.
     *
     * @param visitorId : Unique visitor identifier.
     * @param context   : visitor context.
     * @return Visitor
     */
    public static Visitor newVisitor(String visitorId, HashMap<String, Object> context) {
        if (visitorId == null || visitorId.length() <= 0) {
            visitorId = UUID.randomUUID().toString();
            FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, FlagshipConstants.Warnings.VISITOR_ID_NULL_OR_EMPTY);
        }
        else if (getStatus() == Status.NOT_READY)
            FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, String.format(FlagshipConstants.Warnings.VISITOR_STATUS_NOT_READY, visitorId, getStatus()));
        return new Visitor(getConfig(), getDecisionManager(), visitorId, (context != null) ? context : new HashMap<String, Object>());
    }
}
