package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.IFlagshipEndpoints;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DecisionManager implements IDecisionManager, IFlagshipEndpoints {

    private boolean             panic = false;

    DecisionManager() {}

    protected ArrayList<Campaign> parseCampaigns(String json) {
        return Campaign.parse(json);
    }

    public HashMap<String, Modification> getModifications(Flagship.Mode mode, ArrayList<Campaign> campaigns) {
        HashMap<String, Modification> modifications = new HashMap<String, Modification>();
        campaigns.forEach(campaign -> {
            HashMap<String, Modification> campaignModifications = campaign.getModifications(mode);
            modifications.putAll(campaignModifications);
        });
        return modifications;
    }

    public boolean isPanic() {
        return panic;
    }

    protected void setPanic(boolean panic) {
        this.panic = panic;
    }
}
