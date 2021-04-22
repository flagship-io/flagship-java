package com.abtasty.flagship.decision;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

public class ApiManager extends DecisionManager {

    @Override
    public ArrayList<Campaign> getCampaigns(String envId, String visitorId, ConcurrentMap<String, Object> context) {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("x-api-key", config.getApiKey());
        headers.put("x-sdk-client", "java");
        headers.put("x-sdk-version", BuildConfig.flagship_version_name);
        JSONObject json = new JSONObject();
        JSONObject jsonContext = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            jsonContext.put(e.getKey(), e.getValue());
        }
        json.put("visitorId", visitorId);
        json.put("trigger_hit", false);
        json.put("context", jsonContext);
        ArrayList<Campaign> campaigns = new ArrayList<Campaign>();
        try {
            Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.POST,
                    DECISION_API + envId + CAMPAIGNS,
                    headers,
                    json.toString(),
                    config.getTimeout());
            logResponse(response);
            ArrayList<Campaign> newCampaigns = parseCampaignsResponse(response.getResponseContent());
            if (newCampaigns != null)
                campaigns.addAll(newCampaigns);
        } catch (Exception e) {
            e.printStackTrace();
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.ERROR, e.getMessage());
        }
        return campaigns;
    }
}
