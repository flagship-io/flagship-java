package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.HttpHelper;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.main.Visitor;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.function.Function;

public class ApiManager extends DecisionManager {

    public ApiManager(FlagshipConfig config) {
        super(config);
    }

    @Override
    public void getCampaigns(String visitorId, HashMap<String, Object> context) {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("x-api-key", config.apiKey);
        JSONObject json = new JSONObject();
        JSONObject jsonContext = new JSONObject();
        for (HashMap.Entry<String, Object> e : context.entrySet()) {
            jsonContext.put(e.getKey(), e.getValue());
        }
        json.put("visitorId", visitorId);
        json.put("trigger_hit", false);
        json.put("context", jsonContext);
        HttpHelper.sendAsyncHttpRequest(HttpHelper.RequestType.POST, DECISION_API + config.envId + CAMPAIGNS, headers, json.toString(), new HttpHelper.IResponse() {
                    @Override
                    public void onSuccess(Response response) {
                        System.out.println("SUCCESS : " + response.responseContent);
                    }

                    @Override
                    public void onFailure(Response response) {
                        System.out.println("FAIL : " + response.responseContent);
                    }

                    @Override
                    public void onException(Exception e) {
                        System.out.println("EXCEPTION : " + e.getMessage());
                    }
                }
        );
    }

    @Override
    public void getModifications() {

    }
}
