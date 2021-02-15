package com.abtasty.flagship.api;

import com.abtasty.flagship.hits.Activate;
import com.abtasty.flagship.hits.Hit;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONObject;
import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.util.logging.Level;

public class TrackingManager implements IFlagshipEndpoints {

    public TrackingManager() {
    }

    public void sendHit(String visitorId, Hit hit) {
        String endpoint = hit instanceof Activate ? DECISION_API + ACTIVATION : ARIANE;
        JSONObject data = hit.getData();
        data.put(FlagshipConstants.HitKeyMap.VISITOR_ID, visitorId);
        if (hit.checkData()) {
            HttpHelper.sendAsyncHttpRequest(HttpHelper.RequestType.POST, endpoint, null, data.toString(), new HttpHelper.IResponse() {
                @Override
                public void onSuccess(Response response) {
                    logHit(hit, response);
                }

                @Override
                public void onFailure(Response response) {
                    logHit(hit, response);
                }

                @Override
                public void onException(Exception e) {
                }
            });
        } else {
            LogManager.log(LogManager.Tag.TRACKING, LogLevel.ERROR, String.format(FlagshipConstants.Errors.HIT_INVALID_DATA_ERROR, hit.getType(), hit.toString()));
        }
    }

    private void logHit(Hit h, Response response) {
        LogManager.Tag tag = (h instanceof Activate) ? LogManager.Tag.ACTIVATE : LogManager.Tag.TRACKING;
        LogLevel level = response.isSuccess() ? LogLevel.INFO : LogLevel.ERROR;
        StringBuilder content = new StringBuilder();
        content.append(" [" + response.getType() + "] ")
                .append(" " + response.getRequestUrl() + " ")
                .append(" [" + response.getResponseCode() + "] ")
                .append("\n")
                .append(h.getData().toString(2));
        LogManager.log(tag, level, content.toString());
    }
}
