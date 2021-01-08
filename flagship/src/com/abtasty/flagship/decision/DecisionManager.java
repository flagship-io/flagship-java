package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.IFlagshipEndpoints;
import com.abtasty.flagship.main.FlagshipConfig;

public abstract class DecisionManager implements IDecisionManager, IFlagshipEndpoints {

    protected FlagshipConfig config = null;

    DecisionManager(FlagshipConfig config) {
        this.config = config;
    }
}
