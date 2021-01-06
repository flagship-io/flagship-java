package com.abtasty.flagship.main;

import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;

public class FlagshipConfig {

    public String envId = null;
    public String apiKey = null;
    public Flagship.Mode decisionMode = Flagship.Mode.DECISION_API;
    public LogManager logManager = new LogManager(Flagship.Log.ALL);

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