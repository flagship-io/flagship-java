package com.abtasty.flagship.main;

import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.utils.FlagshipExceptionHandler;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.utils.FlagshipConstants;
import java.util.HashMap;

public class Flagship {

    private static Flagship instance = null;

    FlagshipConfig config = null;
    FlagshipExceptionHandler handler = null;
    DecisionManager decisionManager = null;

    Flagship(FlagshipConfig config) {
        if (config != null)
            this.config = config;
        decisionManager = new ApiManager(this.config); //todo conditional configuration for bucketing
//        handler = new FlagshipExceptionHandler(this.config, Thread.getDefaultUncaughtExceptionHandler());
    }

    public enum Mode {
        DECISION_API,
//        BUCKETING,
    }

    public enum Log {
        NONE, ALL, ERRORS
    }

    private static Flagship instance() {
        return instance(null);
    }

    protected static Flagship instance(FlagshipConfig config) {
        if (instance == null) {
            synchronized (Flagship.class) {
                Flagship inst = instance;
                if (inst == null) {
                    synchronized (Flagship.class) {
                        instance = new Flagship(config);
                    }
                }
            }
        }
        return instance;
    }

    public static void start(String envId, String apiKey) {
        start(envId, apiKey, new FlagshipConfig());
    }

    public static void start(String envId, String apiKey, FlagshipConfig config) {
        if (config == null)
            config = new FlagshipConfig();
        config.withEnvId(envId).withApiKey(apiKey);
        if (config.envId == null || config.apiKey == null)
            config.logManager.onLog(LogManager.Tag.INITIALIZATION, LogLevel.ERROR, FlagshipConstants.INITIALIZATION_PARAM_ERROR);
        else
            instance(config);
    }

    public static Boolean isReady() {
        if (instance == null || instance.config == null || instance.config.apiKey == null || instance.config.envId == null)
            return false;
        return true;
    }

    private static FlagshipConfig getConfig() {
        return instance().config;
    }

    public static Visitor newVisitor(String visitorId) {
        return newVisitor(visitorId, null);
    }

    public static Visitor newVisitor(String visitorId, HashMap<String, Object> context) {
        if (isReady()) {
            Visitor visitor = new Visitor(getConfig(), visitorId, (context != null) ? context : new HashMap());
            visitor.setDecisionManager(instance().decisionManager);
            return visitor;
        }
        return null;
    }
}
