package com.abtasty.flagship.decision;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpHelper;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class ApiManager extends DecisionManager {

    public ApiManager() {
    }

    @Override
    public ArrayList<Campaign> getCampaigns(String envId, String visitorId, HashMap<String, Object> context) {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("x-api-key", Flagship.getConfig().getApiKey());
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
            Response response = HttpHelper.sendHttpRequest(HttpHelper.RequestType.POST,
                    DECISION_API + envId + CAMPAIGNS,
                    headers,
                    json.toString(),
                    Flagship.getConfig().getTimeout());
//            if (response != null) {
//                logResponse(response);
//                setPanic(checkPanicResponse(response.getResponseContent()));
//                if (!isPanic()) {
//                    ArrayList<Campaign> newCampaigns = parseCampaigns(response.getResponseContent());
//                    if (newCampaigns != null)
//                        campaigns.addAll(newCampaigns);
//                } else
//                    FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, Level.WARNING, FlagshipConstants.Errors.PANIC);
//            }
            logResponse(response);
            ArrayList<Campaign> newCampaigns = parseCampaignsResponse(response.getResponseContent());
            if (newCampaigns != null)
                campaigns.addAll(newCampaigns);
        } catch (IOException e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, Level.SEVERE, e.getMessage());
        }
        return campaigns;
    }

//    private boolean checkPanicResponse(String content) {
//        try {
//            JSONObject json = new JSONObject(content);
//            return json.has("panic");
//        } catch (Exception e) {
//            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, Level.SEVERE, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
//        }
//        return false;
//    }

    private void logResponse(Response response) {

        String content = "";
        try {
            content = new JSONObject(response.getResponseContent()).toString(2);
        } catch (Exception e) {
            content = response.getResponseContent();
        }
        StringBuilder message = new StringBuilder();
        message.append("[" + response.getType() + "]")
                .append(" " + response.getRequestUrl() + " ")
                .append("[" + response.getResponseCode() + "]")
                .append("\n")
                .append(content);
        FlagshipLogManager.log(FlagshipLogManager.Tag.CAMPAIGNS, response.isSuccess() ? Level.INFO : Level.SEVERE, message.toString());
    }
}
