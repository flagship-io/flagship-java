package com.abtasty.flagship.api;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.VisitorDelegate;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TrackingManager implements IFlagshipEndpoints {

    public TrackingManager() {
    }

    private void sendActivation(VisitorDelegate visitor, Activate hit) {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-sdk-client", "java");
        headers.put("x-sdk-version", BuildConfig.flagship_version_name);
        JSONObject data = hit.getData();
        if (!visitor.getId().isEmpty() && visitor.getAnonymousId() != null) {
            data.put(FlagshipConstants.HitKeyMap.ANONYMOUS_ID, visitor.getAnonymousId());
            data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getId());
        } else if (!visitor.getId().isEmpty() && visitor.getAnonymousId() == null) {
            data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getId());
            data.put(FlagshipConstants.HitKeyMap.ANONYMOUS_ID, JSONObject.NULL);
        } else {
            data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getAnonymousId());
            data.put(FlagshipConstants.HitKeyMap.ANONYMOUS_ID, JSONObject.NULL);
        }
        CompletableFuture<Response> response = HttpManager.getInstance().sendAsyncHttpRequest(HttpManager.RequestType.POST, DECISION_API + ACTIVATION, headers, data.toString());
        response.whenComplete((httpResponse, error) -> logHit(hit, httpResponse));
    }

    public void sendHit(VisitorDelegate visitor, Hit<?> hit) {
        if (hit instanceof Activate)
            sendActivation(visitor, (Activate) hit);
        else {
            if (hit.checkData()) {
                JSONObject data = hit.getData();
                if (!visitor.getId().isEmpty() && visitor.getAnonymousId() != null) {
                    data.put(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID, visitor.getId());
                    data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getAnonymousId());
                } else if (!visitor.getId().isEmpty() && visitor.getAnonymousId() == null) {
                    data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getId());
                    data.put(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID, JSONObject.NULL);
                } else {
                    data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getAnonymousId());
                    data.put(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID, JSONObject.NULL);
                }
                CompletableFuture<Response> response = HttpManager.getInstance().sendAsyncHttpRequest(HttpManager.RequestType.POST, ARIANE, null, data.toString());
                response.whenComplete((httpResponse, error) -> logHit(hit, httpResponse));
            } else
                FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.HIT_INVALID_DATA_ERROR, hit.getType(), hit));
        }
    }

    private void logHit(Hit<?> h, Response response) {
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
            CompletableFuture<Response> response = HttpManager.getInstance().sendAsyncHttpRequest(HttpManager.RequestType.POST, endpoint, null, body.toString());
            response.whenComplete((httpResponse, error) -> {
                String log = String.format("[%s] %s [%d] [%dms]\n%s", httpResponse.getType(), httpResponse.getRequestUrl(),
                        httpResponse.getResponseCode(), httpResponse.getResponseTime(), httpResponse.getRequestContentAsJson().toString(2));
                LogManager.Level level = httpResponse.isSuccess() ? LogManager.Level.DEBUG : LogManager.Level.ERROR;
                FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, level, log);
            });
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
    }
}
