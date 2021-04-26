package com.abtasty.flagship.main;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import java.util.HashMap;

/**
 * Flagship main singleton.
 */
public class Flagship {

    private static volatile Flagship    instance = null;

    private FlagshipConfig              config = null;
    private Status                      status = Status.NOT_READY;

    public enum Mode {
        DECISION_API,
//        BUCKETING,
    }

    public enum Status {
        /**
         * Flaghsip SDK has not been started or initialized successfully.
         */
        NOT_READY,
        /**
         * Flagship SDK is ready to use.
         */
        READY
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

    protected void setConfig(FlagshipConfig config) {
        if (config != null) {
            this.config = config;
        }
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
        if (config == null)
            config = new FlagshipConfig(envId, apiKey);
        config.withEnvId(envId);
        config.withApiKey(apiKey);
        if (config.getEnvId() == null || config.getApiKey() == null)
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, LogManager.Level.ERROR, FlagshipConstants.Errors.INITIALIZATION_PARAM_ERROR);
        instance().setConfig(config);
        if (isReady()) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, LogManager.Level.INFO, String.format(FlagshipConstants.Info.STARTED, BuildConfig.flagship_version_name));
            instance().setStatus(Status.READY);
        } else
            instance().setStatus(Status.NOT_READY);
    }

    public static Status getStatus() {
        return instance().status;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    private static Boolean isReady() {
        return instance != null &&
                instance.config != null &&
                instance.config.getApiKey() != null &&
                instance.config.getEnvId() != null &&
                instance.config.getDecisionManager() != null &&
                HttpManager.getInstance().isReady();
    }

    /**
     * Return the current used configuration.
     *
     * @return FlagshipConfig
     */
    public static FlagshipConfig getConfig() {
        return instance().config;
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
        if (isReady() && visitorId != null)
            return new Visitor(getConfig(), visitorId, (context != null) ? context : new HashMap<String, Object>());
        return null;
    }
}
