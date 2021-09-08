package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.IFlagshipEndpoints;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.ArrayList;

public abstract class DecisionManager implements IDecisionManager, IFlagshipEndpoints {

    protected final     FlagshipConfig<?>              config;
    private             boolean                        panic           = false;
    protected           Flagship.StatusListener        statusListener  = null;

    public DecisionManager(FlagshipConfig<?> config) {
        this.config = config;
    }

    protected ArrayList<Campaign> parseCampaignsResponse(String content) {
        if (content != null && !content.isEmpty()) {
            try {
                JSONObject json = new JSONObject(content);
                panic = json.has("panic");
                updateFlagshipStatus((panic) ? Flagship.Status.PANIC : Flagship.Status.READY);
                if (!panic)
                    return Campaign.parse(json.getJSONArray("campaigns"));
            } catch (Exception e) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
            }
        }
        return null;
    }

    protected void updateFlagshipStatus(Flagship.Status newStatus) {
        if (statusListener != null && Flagship.getStatus() != newStatus)
            statusListener.onStatusChanged(newStatus);
        if (newStatus == Flagship.Status.PANIC)
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.WARNING, FlagshipConstants.Warnings.PANIC);
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

    public abstract void stop();
}
