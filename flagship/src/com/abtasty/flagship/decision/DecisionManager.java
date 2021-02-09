package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.IFlagshipEndpoints;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;

import java.util.ArrayList;

public abstract class DecisionManager implements IDecisionManager, IFlagshipEndpoints {

    protected FlagshipConfig config = null;

    DecisionManager(FlagshipConfig config) {
        this.config = config;
    }

    protected ArrayList<Campaign> parseCampaigns(String json) {
        return Campaign.parse(json);
    }
}
