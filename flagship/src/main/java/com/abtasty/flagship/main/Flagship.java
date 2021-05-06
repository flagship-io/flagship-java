package com.abtasty.flagship.main;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.BucketingManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.Visitor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Flagship main singleton.
 */
public class Flagship {

    private static volatile Flagship    instance = null;

    private FlagshipConfig              config = FlagshipConfig.emptyConfig();
    private DecisionManager             decisionManager = null;
    private Status                      status = Status.NOT_INITIALIZED;

    public enum Mode {
        DECISION_API,
        BUCKETING,
    }

    public enum Status {
        /**
         * Flagship SDK has not been started or initialized successfully.
         */
        NOT_INITIALIZED(0x0),
        /**
         * Flagship SDK is starting.
         */
        STARTING(0x1),
        /**
         * Flagship SDK has been started successfully but is still polling campaigns.
         */
        POLLING(0x10),
        /**
         * Flagship SDK is ready but is running in Panic mode: All features are disabled except the one which refresh this status.
         */
        READY_PANIC_ON(0x20),
        /**
         * Flagship SDK is ready to use.
         */
        READY(0x100);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public boolean lessThan(Status status) {
            return this.value < status.value;
        }

        public boolean greaterThan(Status status) {
            return this.value > status.value;
        }
    }

    public interface StatusListener {
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
        instance().updateStatus(Status.STARTING);
        if (envId != null && apiKey != null) {
            if (config == null)
                config = new FlagshipConfig(envId, apiKey);
            config.withEnvId(envId);
            config.withApiKey(apiKey);
            instance().setConfig(config);
            DecisionManager decisionManager = (config.getDecisionMode() == Flagship.Mode.DECISION_API) ? new ApiManager(config) : new BucketingManager(config);
            decisionManager.setStatusListener(newStatus -> instance().updateStatus(newStatus));
            instance().setDecisionManager(decisionManager);
        } else {
            instance().updateStatus(Status.NOT_INITIALIZED);
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, LogManager.Level.ERROR, FlagshipConstants.Errors.INITIALIZATION_PARAM_ERROR);
        }
    }

    public static Status getStatus() {
        return instance().status;
    }

    protected void updateStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            FlagshipLogManager.log(FlagshipLogManager.Tag.GLOBAL, LogManager.Level.INFO, (this.status == Status.READY) ?
                            String.format(FlagshipConstants.Info.READY, BuildConfig.flagship_version_name) :
                            String.format(FlagshipConstants.Info.STATUS_CHANGED, status));
            if (this.config != null && this.config.getStatusListener() != null)
                config.getStatusListener().onStatusChanged(status);
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
        else if (!getStatus().greaterThan(Status.POLLING))
            FlagshipLogManager.log(FlagshipLogManager.Tag.VISITOR, LogManager.Level.WARNING, String.format(FlagshipConstants.Warnings.VISITOR_STATUS_NOT_READY, visitorId, getStatus()));
        return new Visitor(getConfig(), getDecisionManager(), visitorId, (context != null) ? context : new HashMap<String, Object>());
    }
}
