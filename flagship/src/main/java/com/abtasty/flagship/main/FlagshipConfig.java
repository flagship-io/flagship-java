package com.abtasty.flagship.main;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.utils.FlagshipLogManager;

/**
 * Flagship SDK configuration class to provide at initialization.
 */
public class FlagshipConfig {

    private String              envId           = null;
    private String              apiKey          = null;
    private Flagship.Mode       decisionMode    = Flagship.Mode.DECISION_API;
    private int                 timeout         = 2000;
    private LogManager.Level    logLevel        = LogManager.Level.ALL;
    private LogManager          logManager      = new FlagshipLogManager(logLevel);
    private TrackingManager     trackingManager = new TrackingManager();
    private DecisionManager     decisionManager = null;


    /**
     * Create a new empty FlagshipConfig configuration.
     */
    public FlagshipConfig() {
        init();
    }

    /**
     * Create a new FlagshipConfig configuration.
     *
     * @param envId : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     */
    protected FlagshipConfig(String envId, String apiKey) {
        this.envId = envId;
        this.apiKey = apiKey;
        init();
    }

    private void init() {
        this.decisionManager = (this.decisionMode == Flagship.Mode.DECISION_API) ? new ApiManager() : null;
    }

    /**
     * Specify the environment id provided by Flagship, to use.
     * @param envId environment id.
     * @return FlagshipConfig
     */
    protected FlagshipConfig withEnvId(String envId) {
        this.envId = envId;
        return this;
    }

    /**
     * Specify the secure api key provided by Flagship, to use.
     * @param apiKey secure api key.
     * @return FlagshipConfig
     */
    protected FlagshipConfig withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Specify the SDK running mode.
     * @param mode mode.
     * @return FlagshipConfig
     */
    public FlagshipConfig withFlagshipMode(Flagship.Mode mode) {
        if (mode != null) {
            this.decisionMode = mode;
            init();
        }
        return this;
    }

    /**
     * Specify a custom implementation of LogManager in order to receive logs from the SDK.
     * @param logManager custom implementation of LogManager.
     * @return FlagshipConfig
     */
    public FlagshipConfig withLogManager(LogManager logManager) {
        this.logManager = logManager;
        return this;
    }

    /**
     * Specify a log level to filter SDK logs.
     * @param level
     * @return FlagshipConfig
     */
    public FlagshipConfig withLogLevel(LogManager.Level level) {
        if (level != null) {
            this.logLevel = level;
            this.logManager.setLevel(this.logLevel);
        }
        return this;
    }

    /**
     * Specify timeout for api request.
     * @param timeout milliseconds for connect and read timeouts. Default is 2000.
     * @return FlagshipConfig
     */
    public FlagshipConfig withTimeout(int timeout) {
        if (timeout > 0)
            this.timeout = timeout;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getEnvId() {
        return envId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Flagship.Mode getDecisionMode() {
        return decisionMode;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    protected TrackingManager getTrackingManager() {
        return trackingManager;
    }

    protected DecisionManager getDecisionManager() {
        return decisionManager;
    }

    @Override
    public String toString() {
        return "FlagshipConfig{" +
                "envId='" + envId + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", mode=" + decisionMode +
                ", logManager=" + logManager +
                '}';
    }
}