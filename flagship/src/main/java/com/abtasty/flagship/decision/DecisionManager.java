package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.IFlagshipEndpoints;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DecisionManager implements IDecisionManager, IFlagshipEndpoints {

    protected final     FlagshipConfig                      config;
    private             boolean                             panic           = false;
    protected           Flagship.StatusListener             statusListener  = null;

    public DecisionManager(FlagshipConfig config) {
        this.config = config;
    }

    protected ArrayList<Campaign> parseCampaignsResponse(String content) {
        try {
            JSONObject json = new JSONObject(content);
            panic = json.has("panic");
            if (!panic)
                return Campaign.parse(json.getJSONArray("campaigns"));
            else {
                if (statusListener != null)
                    statusListener.onStatusChanged(Flagship.Status.READY_PANIC_ON);
                FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.WARNING, FlagshipConstants.Warnings.PANIC);
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
        }
        return null;
    }

    public HashMap<String, Modification> getModifications(ArrayList<Campaign> campaigns) {
        HashMap<String, Modification> modifications = new HashMap<String, Modification>();
        if (campaigns != null)
            campaigns.forEach(campaign -> modifications.putAll(campaign.getModifications()));
        return modifications;
    }

    public boolean isPanic() {
        return panic;
    }

    protected void logResponse(Response response) {

        String content;
        try {
            content = new JSONObject(response.getResponseContent()).toString(2);
        } catch (Exception e) {
            content = response.getResponseContent();
        }
        String message = String.format("[%s] %s [%d] [%dms]\n %s", response.getType(), response.getRequestUrl(),
                response.getResponseCode(), response.getResponseTime(), content);
        FlagshipLogManager.log(FlagshipLogManager.Tag.CAMPAIGNS, response.isSuccess() ?
                LogManager.Level.DEBUG : LogManager.Level.ERROR, message);

    }

    public void setStatusListener(Flagship.StatusListener statusListener) {
        this.statusListener = statusListener;
    }
}
