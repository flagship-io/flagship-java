package com.abtasty.flagship.main;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;

public class FlagshipConfig {

    private String           envId = null;
    private String           apiKey = null;
    private Flagship.Mode    decisionMode = Flagship.Mode.DECISION_API;
    private LogManager       logManager = new LogManager(Flagship.Log.ALL);
    private TrackingManager  trackingManager = new TrackingManager();

    public FlagshipConfig() {
    }

    public FlagshipConfig(String envId, String apiKey) {
        this.envId = envId;
        this.apiKey = apiKey;
    }

    FlagshipConfig withEnvId(String envId) {
        this.envId = envId;
        return this;
    }

    FlagshipConfig withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public FlagshipConfig withFlagshipMode(Flagship.Mode mode) {
        this.decisionMode = mode;
        return this;
    }

    public FlagshipConfig withLogManager(LogManager logManager) {
        if (logManager == null)
            this.logManager = new LogManager(Flagship.Log.NONE);
        else
            this.logManager = logManager;
        return this;
    }

    public Flagship start() {
        return Flagship.instance(this);
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