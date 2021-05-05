package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BucketingManager extends DecisionManager {

    private boolean                         pollingDaemon = true;
    private Thread                          pollingThread = null;
    private String                          last_modified;
    private String                          bucketing_content;

    public BucketingManager(FlagshipConfig config) {
        super(config);
        startPolling();
    }

    @Override
    public ArrayList<Campaign> getCampaigns(String visitorId, HashMap<String, Object> context) {
        ArrayList<Campaign> targetedCampaigns = new ArrayList<>();
        if (bucketing_content != null) {
            ArrayList<Campaign> campaigns = parseCampaignsResponse(bucketing_content);
            if (campaigns != null) {
                for (Campaign campaign : campaigns) {
                    campaign.selectVariation(visitorId);
                    if (campaign.selectedVariationGroupFromTargeting(context))
                        targetedCampaigns.add(campaign);
                }
            }
        }
        return targetedCampaigns;
    }

    public void startPolling() {
        pollingDaemon = true;
        if (pollingThread == null)
            initPollingThread();
        this.pollingThread.start();
    }

    private void sendBucketingRequest() {
        try {
            HashMap<String, String> headers = new HashMap<String, String>();
            if (last_modified != null)
                headers.put("If-Modified-Since", last_modified);
            Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.GET,
                    String.format(BUCKETING, config.getEnvId()), headers, null, config.getTimeout());
            logResponse(response);
            if (response.isSuccess(false)) {
                last_modified = response.getResponseHeader("Last-Modified");
                bucketing_content = response.getResponseContent();
                updateFlagshipStatus();
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.ERROR, e.getMessage());
        }
    }

    private void updateFlagshipStatus() {
        if (onStatusChangedListener != null && Flagship.getStatus() != Flagship.Status.READY)
            onStatusChangedListener.onStatusChanged(Flagship.Status.READY);
    }

    public void stopPolling() {
        pollingDaemon = false;
    }

    private void initPollingThread() {
        this.pollingThread = new Thread(() -> {
            while (pollingDaemon) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING, LogManager.Level.DEBUG, FlagshipConstants.Info.BUCKETING_INTERVAL);
                try {
                    sendBucketingRequest();
                    Thread.sleep(TimeUnit.MILLISECONDS.convert(config.getPollingTime(), config.getPollingUnit()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.pollingThread.setDaemon(true);
    }
}
