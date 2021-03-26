package com.abtasty.flagship.decision;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.utils.FlagshipLogManager;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class ApiManager extends DecisionManager {

    public ApiManager() {
    }

    @Override
    public ArrayList<Campaign> getCampaigns(String envId, String visitorId, ConcurrentMap<String, Object> context) {

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
            Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.POST,
                    DECISION_API + envId + CAMPAIGNS,
                    headers,
                    json.toString());
            ResponseBody body = response.body();
            if (body != null) {
                logResponse(response.request(), response, body.string());
                ArrayList<Campaign> newCampaigns = parseCampaignsResponse(body.string());
                if (newCampaigns != null)
                    campaigns.addAll(newCampaigns);
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, Level.SEVERE, e.getMessage());
        }
        return campaigns;
    }

    private void logResponse(Request request, Response response, String content) {
        try {
            content = new JSONObject(content).toString(2);
        } catch (Exception ignored) { }
        StringBuilder message = new StringBuilder()
                .append("[").append(request.method()).append("]")
                .append(" ").append(request.url()).append(" ")
                .append("[").append(response.code()).append("]")
                .append(" [").append(response.receivedResponseAtMillis() - response.sentRequestAtMillis()).append("ms]")
                .append("\n")
                .append(content);
        FlagshipLogManager.log(FlagshipLogManager.Tag.CAMPAIGNS, response.isSuccessful() ? Level.INFO : Level.SEVERE, message.toString());
    }
}
