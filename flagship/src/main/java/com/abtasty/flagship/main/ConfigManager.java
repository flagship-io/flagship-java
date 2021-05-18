package com.abtasty.flagship.main;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.BucketingManager;
import com.abtasty.flagship.decision.DecisionManager;

/**
 * Flagship main configuration holder.
 */
public class ConfigManager {

    private FlagshipConfig          flagshipConfig  = FlagshipConfig.emptyConfig();
    private DecisionManager         decisionManager = null;
    private final TrackingManager   trackingManager = new TrackingManager();

    public void init(String envId, String apiKey, FlagshipConfig config) {
        if (config == null)
            config = new FlagshipConfig(envId, apiKey);
        config.withEnvId(envId);
        config.withApiKey(apiKey);
        this.flagshipConfig = config;
        this.decisionManager = (config.getDecisionMode() == Flagship.Mode.DECISION_API) ? new ApiManager(config) : new BucketingManager(config);
    }

    public DecisionManager getDecisionManager() {
        return decisionManager;
    }

    public TrackingManager getTrackingManager() {
        return trackingManager;
    }

    public FlagshipConfig getFlagshipConfig() {
        return flagshipConfig;
    }

    public boolean isSet() {
        return  (this.flagshipConfig != null && this.flagshipConfig.isSet()) &&
                (this.decisionManager != null);
    }

    public void reset() {
        if (this.decisionManager != null)
            this.decisionManager.stop();
        this.flagshipConfig = FlagshipConfig.emptyConfig();
        this.decisionManager = null;
    }
}
