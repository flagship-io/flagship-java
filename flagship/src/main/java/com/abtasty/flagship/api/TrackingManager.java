package com.abtasty.flagship.api;

import com.abtasty.flagship.BuildConfig;
import com.abtasty.flagship.cache.CacheHelper;
import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.VisitorDelegateDTO;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TrackingManager implements IFlagshipEndpoints {

    public TrackingManager() {
    }

    private void sendActivation(VisitorDelegateDTO visitor, Activate hit) {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-sdk-client", "java");
        headers.put("x-sdk-version", BuildConfig.flagship_version_name);
        JSONObject data = hit.getData();
        if (!visitor.getVisitorId().isEmpty() && visitor.getAnonymousId() != null) {
            data.put(FlagshipConstants.HitKeyMap.ANONYMOUS_ID, visitor.getAnonymousId());
            data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getVisitorId());
        } else if (!visitor.getVisitorId().isEmpty() && visitor.getAnonymousId() == null) {
            data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getVisitorId());
            data.put(FlagshipConstants.HitKeyMap.ANONYMOUS_ID, JSONObject.NULL);
        } else {
            data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getAnonymousId());
            data.put(FlagshipConstants.HitKeyMap.ANONYMOUS_ID, JSONObject.NULL);
        }
        sendTracking(visitor,  hit.getType() + "", DECISION_API + ACTIVATION, headers, data, -1);
    }

    public void sendHit(VisitorDelegateDTO visitor, Hit<?> hit) {
        if (hit instanceof Activate)
            sendActivation(visitor, (Activate) hit);
        else {
            if (hit.checkData()) {
                JSONObject data = hit.getData();
                if (!visitor.getVisitorId().isEmpty() && visitor.getAnonymousId() != null) {
                    data.put(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID, visitor.getVisitorId());
                    data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getAnonymousId());
                } else if (!visitor.getVisitorId().isEmpty() && visitor.getAnonymousId() == null) {
                    data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getVisitorId());
                    data.put(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID, JSONObject.NULL);
                } else {
                    data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitor.getAnonymousId());
                    data.put(FlagshipConstants.HitKeyMap.CUSTOM_VISITOR_ID, JSONObject.NULL);
                }
                sendTracking(visitor, hit.getType() + "", ARIANE, null, data, -1);
            } else
                FlagshipLogManager.log(FlagshipLogManager.Tag.TRACKING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.HIT_INVALID_DATA_ERROR, hit.getType(), hit));
        }
    }

    public void sendHit(VisitorDelegateDTO visitorDelegateDTO, String type, long time, JSONObject content) {
        String endpoint = null;
        HashMap<String, String> headers = new HashMap<String, String>();
        if (type.equals("CONTEXT")) {
            endpoint = DECISION_API + visitorDelegateDTO.getConfigManager().getFlagshipConfig().getEnvId() + EVENTS;
            headers.put("x-sdk-client", "android");
            headers.put("x-sdk-version", BuildConfig.flagship_version_name);
        }
        else if (type.equals("ACTIVATION")) {
            endpoint = DECISION_API + ACTIVATION;
            headers.put("x-sdk-client", "android");
            headers.put("x-sdk-version", BuildConfig.flagship_version_name);
        }
        else if (Arrays.asList("SCREENVIEW", "PAGEVIEW", "EVENT", "TRANSACTION", "ITEM", "CONSENT", "BATCH").contains(type)) {
            endpoint = ARIANE;
        }
        if (endpoint != null)
            sendTracking(visitorDelegateDTO, type, endpoint, headers, content, time);
    }

    private void sendTracking(VisitorDelegateDTO visitorDelegateDTO, String type, String endpoint, HashMap<String, String> headers, JSONObject content, long time) {
        HttpManager.getInstance().sendAsyncHttpRequest(HttpManager.RequestType.POST, endpoint, headers, content.toString())
                .whenComplete((response, error) -> {
                    FlagshipLogManager.Tag tag = (type.equals(FlagshipLogManager.Tag.ACTIVATE.name())) ? FlagshipLogManager.Tag.ACTIVATE : FlagshipLogManager.Tag.TRACKING;
                    if (response != null)
                        logHit(tag, response, response.getRequestContent());
                    if (response == null || response.getResponseCode() < 200 || response.getResponseCode() > 204) {
                        JSONObject json = CacheHelper.fromHit(visitorDelegateDTO, type, content, time);
                        visitorDelegateDTO.getVisitorDelegate().getStrategy().cacheHit(visitorDelegateDTO.getVisitorId(), json);
                    }
                });
    }

    private void logHit(FlagshipLogManager.Tag tag, Response response, String content) {
        try {
            LogManager.Level level = (response.getResponseCode() < 400) ? LogManager.Level.DEBUG : LogManager.Level.ERROR;
            String log = String.format("[%s] %s [%d] [%dms]\n%s", "POST", response.getRequestUrl(),
                    response.getResponseCode(), response.getResponseTime(), new JSONObject(content).toString(3));
            FlagshipLogManager.log(tag, level, log);
        } catch (Exception ignored) {}
    }

    public void sendContextRequest(VisitorDelegateDTO visitor) {
        try {
            String endpoint = DECISION_API + visitor.getConfigManager().getFlagshipConfig().getEnvId() + EVENTS;
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("x-sdk-client", "android");
            headers.put("x-sdk-version", BuildConfig.flagship_version_name);

            JSONObject data = new JSONObject();
            for (Map.Entry<String, Object> item : visitor.getContext().entrySet()) {
                data.put(item.getKey(), item.getValue());
            }
            JSONObject body = new JSONObject()
                    .put("visitorId", visitor.getVisitorId())
                    .put("type", "CONTEXT")
                    .put("data", data);
            sendTracking(visitor, "CONTEXT", endpoint, headers, body, -1);
        } catch (Exception e) {
            FlagshipLogManager.exception(e);
        }
    }
}
