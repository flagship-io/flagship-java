package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.IFlagshipEndpoints;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public abstract class DecisionManager implements IDecisionManager, IFlagshipEndpoints {

    private boolean             panic = false;

    DecisionManager() {}

    protected ArrayList<Campaign> parseCampaignsResponse(String content) {
        try {
            JSONObject json = new JSONObject(content);
            panic = json.has("panic");
            if (!panic)
                return Campaign.parse(json.getJSONArray("campaigns"));
            else
                FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, Level.WARNING, FlagshipConstants.Errors.PANIC);
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, Level.SEVERE, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
        }
        return null;
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
//
//    protected void setPanic(boolean panic) {
//        this.panic = panic;
//    }
}
