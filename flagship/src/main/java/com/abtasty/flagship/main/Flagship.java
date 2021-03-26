package com.abtasty.flagship.main;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipExecutorService;
import com.abtasty.flagship.utils.FlagshipLogManager;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Flagship main singleton.
 */
public class Flagship {

    private static volatile Flagship    instance = null;

    private FlagshipConfig              config = null;

    public enum Mode {
        DECISION_API,
//        BUCKETING,
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
        System.out.println("RUNTIME");
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("SHUT DOWN");
//            FlagshipExecutorService.getInstance().service().shutdownNow();
//            FlagshipExecutorService.getInstance().closeService();
//        }));
        if (config == null)
            config = new FlagshipConfig(envId, apiKey);
        config.withEnvId(envId);
        config.withApiKey(apiKey);
        if (config.getEnvId() == null || config.getApiKey() == null)
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, Level.SEVERE, FlagshipConstants.Errors.INITIALIZATION_PARAM_ERROR);
        instance().setConfig(config);
        if (isReady()) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.INITIALIZATION, Level.INFO, String.format(FlagshipConstants.Info.STARTED, BuildConfig.flagship_version_name));
        }
    }

    /**
     * Check if the SDK is ready to use.
     *
     * @return boolean ready.
     */
    public static Boolean isReady() {
        return instance != null &&
                instance.config != null &&
                instance.config.getApiKey() != null &&
                instance.config.getEnvId() != null &&
                instance.config.getDecisionManager() != null;
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
