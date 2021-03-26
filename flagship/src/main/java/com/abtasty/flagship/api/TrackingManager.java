package com.abtasty.flagship.api;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

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
            response.whenComplete((httpResponse, error) -> {
                logHit(hit, httpResponse);
            });
        } else {
            FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, Level.SEVERE, String.format(FlagshipConstants.Errors.HIT_INVALID_DATA_ERROR, hit.getType(), hit.toString()));
        }
    }

    private void logHit(Hit h, Response response) {
        FlagshipLogManager.Tag tag = (h instanceof Activate) ? FlagshipLogManager.Tag.ACTIVATE : FlagshipLogManager.Tag.TRACKING;
        Level level = response.isSuccessful() ? Level.INFO : Level.SEVERE;
        StringBuilder content = new StringBuilder();
        Request request = response.request();
        content.append(" [").append(request.method()).append("] ")
                .append(" ").append(request.url()).append(" ")
                .append(" [").append(response.code()).append("] ")
                .append("\n")
                .append(h.getData().toString(2));
        FlagshipLogManager.log(tag, level, content.toString());
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
