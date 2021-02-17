package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.HttpHelper;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class ApiManager extends DecisionManager {

    public ApiManager(FlagshipConfig config) {
        super(config);
    }

    @Override
    public ArrayList<Campaign> getCampaigns(String visitorId, HashMap<String, Object> context) {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("x-api-key", config.getApiKey());
        JSONObject json = new JSONObject();
        JSONObject jsonContext = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            jsonContext.put(e.getKey(), e.getValue());
        }
        json.put("visitorId", visitorId);
        json.put("trigger_hit", false);
        json.put("context", jsonContext);
        ArrayList<Campaign> campaigns  = new ArrayList();
        try {
            Response response = HttpHelper.sendHttpRequest(HttpHelper.RequestType.POST, DECISION_API + config.getEnvId() + CAMPAIGNS, headers, json.toString());
            if (response != null) {
                logResponse(response);
                setPanic(checkPanicResponse(response.getResponseContent()));
                if (!isPanic()) {
                    ArrayList<Campaign> newCampaigns = parseCampaigns(response.getResponseContent());
                    campaigns.addAll(newCampaigns);
                } else
                    LogManager.log(LogManager.Tag.SYNCHRONIZE, LogLevel.WARNING, FlagshipConstants.Errors.PANIC);
            }
        } catch (IOException e) {
            LogManager.log(LogManager.Tag.SYNCHRONIZE, LogLevel.ERROR, e.getMessage());
        }
        return campaigns;
    }

    private boolean checkPanicResponse(String content) {
        try {
            JSONObject json = new JSONObject(content);
            return json.has("panic");
        } catch (Exception e) {
            LogManager.log(LogManager.Tag.PARSING, LogLevel.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
        }
        return false;
    }

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
        LogManager.log(LogManager.Tag.CAMPAINGS, response.isSuccess() ? LogLevel.INFO : LogLevel.ERROR, message.toString());
    }
}
