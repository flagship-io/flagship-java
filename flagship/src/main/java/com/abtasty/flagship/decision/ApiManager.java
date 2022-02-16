package com.abtasty.flagship.decision;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.model.Variation;
import com.abtasty.flagship.model.VariationGroup;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.VisitorDelegate;
import com.abtasty.flagship.visitor.VisitorDelegateDTO;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ApiManager extends DecisionManager {

    public ApiManager(FlagshipConfig<?> config) {
        super(config);
    }

    @Override
    public void setStatusListener(Flagship.StatusListener statusListener) {
        super.setStatusListener(statusListener);
        if (Flagship.getStatus().lessThan(Flagship.Status.READY))
            statusListener.onStatusChanged(Flagship.Status.READY);
    }

    private ArrayList<Campaign> sendCampaignRequest(VisitorDelegateDTO visitor) throws IOException {
        JSONObject json = new JSONObject();
        HashMap<String, String> headers = new HashMap<String, String>() {{
            put("x-api-key", config.getApiKey());
            put("x-sdk-client", "java");
            put("x-sdk-version", BuildConfig.flagship_version_name);
        }};
        json.put("visitorId", visitor.getVisitorId());
        json.put("anonymousId", visitor.getAnonymousId());
        json.put("trigger_hit", false);
        json.put("context", visitor.contextToJson());
        Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.POST,
                DECISION_API + config.getEnvId() + CAMPAIGNS + ((!visitor.hasConsented()) ? CONTEXT_PARAM : ""),
                headers,
                json.toString(),
                config.getTimeout());
        logResponse(response);
        return (response.isSuccess()) ? parseCampaignsResponse(response.getResponseContent()) : null;
    }

    @Override
    public HashMap<String, Modification> getCampaignsModifications(VisitorDelegateDTO visitor) {
        try {
            ArrayList<Campaign> campaigns = sendCampaignRequest(visitor);
            if (campaigns != null) {
                HashMap<String, Modification> campaignsModifications = new HashMap<>();
                for (Campaign campaign : campaigns) {
                    for (VariationGroup variationGroup : campaign.getVariationGroups()) {
                        for (Variation variation : variationGroup.getVariations().values()) {
                            HashMap<String, Modification> modificationsValues = variation.getModificationsValues();
                            if (modificationsValues != null)
                                campaignsModifications.putAll(modificationsValues);
                        }
                    }
                }
                return campaignsModifications;
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public void stop() { }
}
