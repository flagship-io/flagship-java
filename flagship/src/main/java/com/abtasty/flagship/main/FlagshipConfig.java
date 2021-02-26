package com.abtasty.flagship.main;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.DecisionManager;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;

/**
 * Flagship SDK configuration class to provide at initialization.
 */
public class FlagshipConfig {

    private String          envId = null;
    private String          apiKey = null;
    private Flagship.Mode   decisionMode = Flagship.Mode.DECISION_API;
    private LogManager      logManager = new LogManager(Flagship.Log.ALL);
    private TrackingManager trackingManager = new TrackingManager();

    /**
     * Create a new empty FlagshipConfig configuration.
     */
    public FlagshipConfig() {}

    /**
     * Create a new FlagshipConfig configuration.
     *
     * @param envId : Environment id provided by Flagship.
     * @param apiKey : Secure api key provided by Flagship.
     */
    public FlagshipConfig(String envId, String apiKey) {
        this.envId = envId;
        this.apiKey = apiKey;
    }

    /**
     * Specify the environment id provided by Flagship, to use.
     * @param envId environment id.
     * @return FlagshipConfig
     */
    FlagshipConfig withEnvId(String envId) {
        this.envId = envId;
        return this;
    }

    /**
     * Specify the secure api key provided by Flagship, to use.
     * @param apiKey secure api key.
     * @return FlagshipConfig
     */
    FlagshipConfig withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Specify the SDK running mode.
     * @param mode mode.
     * @return FlagshipConfig
     */
    public FlagshipConfig withFlagshipMode(Flagship.Mode mode) {
        this.decisionMode = mode;
        return this;
    }

    /**
     * Specify a custom implementation of LogManager in order to receive logs from the SDK.
     * @param logManager custom implementation of LogManager.
     * @return FlagshipConfig
     */
    public FlagshipConfig withLogManager(LogManager logManager) {
        if (logManager == null)
            this.logManager = new LogManager(Flagship.Log.NONE);
        else
            this.logManager = logManager;
        return this;
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

    public TrackingManager getTrackingManager() {
        return trackingManager;
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