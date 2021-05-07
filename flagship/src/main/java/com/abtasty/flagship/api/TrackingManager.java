package com.abtasty.flagship.api;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TrackingManager implements IFlagshipEndpoints {

    public TrackingManager() {
    }

    public void sendHit(String visitorId, Hit hit) {
        this.sendHit(ARIANE, null, visitorId, hit);
    }

    public void sendActivation(String visitorId, Activate hit) {
        String endPoint = DECISION_API + ACTIVATION;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-sdk-client", "java");
        headers.put("x-sdk-version", BuildConfig.flagship_version_name);
        sendHit(endPoint, headers, visitorId, hit);
    }

    private void sendHit(String endpoint, HashMap<String, String> headers, String visitorId, Hit hit) {
        JSONObject data = hit.getData();
        data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitorId);
        if (hit.checkData()) {
            CompletableFuture<Response> response = HttpManager.getInstance().sendAsyncHttpRequest(HttpManager.RequestType.POST, endpoint, headers, data.toString());
            response.whenComplete((httpResponse, error) -> logHit(hit, httpResponse));
        } else {
            FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.HIT_INVALID_DATA_ERROR, hit.getType(), hit));
        }
    }

    private void logHit(Hit h, Response response) {
        FlagshipLogManager.Tag tag = (h instanceof Activate) ? FlagshipLogManager.Tag.ACTIVATE : FlagshipLogManager.Tag.TRACKING;
        LogManager.Level level = response.isSuccess() ? LogManager.Level.DEBUG : LogManager.Level.ERROR;
        String log = String.format("[%s] %s [%d] [%dms]\n%s", response.getType(), response.getRequestUrl(),
                response.getResponseCode(), response.getResponseTime(), h.getData().toString(2));
        FlagshipLogManager.log(tag, level, log);
    }

    public void sendContextRequest(String envId, String visitorId, HashMap<String, Object> context) {
        try {
            String endpoint = DECISION_API + envId + EVENTS;
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            body.put("visitorId", visitorId);
            body.put("type", "CONTEXT");
            for (Map.Entry<String, Object> item : context.entrySet()) {
                data.put(item.getKey(), item.getValue());
            }
            body.put("data", data);
            HttpManager.getInstance().sendAsyncHttpRequest(HttpManager.RequestType.POST, endpoint, null, body.toString());
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
    }
}
