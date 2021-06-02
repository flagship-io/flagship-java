package com.abtasty.flagship.main;

import com.abtasty.flagship.api.TrackingManager;
import com.abtasty.flagship.decision.ApiManager;
import com.abtasty.flagship.decision.BucketingManager;
import com.abtasty.flagship.decision.DecisionManager;

/**
 * Flagship main configuration holder.
 */
public class ConfigManager {

    private FlagshipConfig<?>                   flagshipConfig  = new FlagshipConfig.DecisionApi();
    private DecisionManager                     decisionManager = null;
    private final TrackingManager               trackingManager = new TrackingManager();

    public void init(String envId, String apiKey, FlagshipConfig<?> config) {
        if (config == null)
            config = new FlagshipConfig.DecisionApi(envId, apiKey);
        config.withEnvId(envId);
        config.withApiKey(apiKey);
        this.flagshipConfig = config;
        this.decisionManager = (config.getDecisionMode() == Flagship.DecisionMode.API) ? new ApiManager(config) : new BucketingManager(config);
    }

    public DecisionManager getDecisionManager() {
        return decisionManager;
    }

    public TrackingManager getTrackingManager() {
        return trackingManager;
    }

    public FlagshipConfig<?> getFlagshipConfig() {
        return flagshipConfig;
    }

    public boolean isSet() {
        return  (this.flagshipConfig != null && this.flagshipConfig.isSet()) &&
                (this.decisionManager != null);
    }

    public boolean isDecisionMode(Flagship.DecisionMode mode) {
        return this.flagshipConfig.getDecisionMode() == mode;
    }

    public void reset() {
        if (this.decisionManager != null)
            this.decisionManager.stop();
//        this.flagshipConfig = new FlagshipConfig.DecisionApi();
        this.decisionManager = null;
    }
}
