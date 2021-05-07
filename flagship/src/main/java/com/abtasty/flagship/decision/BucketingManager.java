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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BucketingManager extends DecisionManager {

    private String                          last_modified;
    private String                          bucketing_content;
    private ScheduledExecutorService        executor;

    public BucketingManager(FlagshipConfig config) {
        super(config);
        startPolling();
    }

    @Override
    public void setStatusListener(Flagship.StatusListener statusListener) {
        super.setStatusListener(statusListener);
        if (Flagship.getStatus().lessThan(Flagship.Status.READY))
            statusListener.onStatusChanged(Flagship.Status.POLLING);
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
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });
            executor.scheduleAtFixedRate(() -> {
                FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING,
                        LogManager.Level.DEBUG,
                        FlagshipConstants.Info.BUCKETING_INTERVAL);
                sendBucketingRequest();
            }, 0, config.getPollingTime(), config.getPollingUnit());
        }
    }

    private void sendBucketingRequest() {
        try {
            HashMap<String, String> headers = new HashMap<String, String>();
            if (last_modified != null)
                headers.put("If-Modified-Since", last_modified);
            Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.GET,
                    String.format(BUCKETING, config.getEnvId()),
                    headers,
                    null,
                    config.getTimeout());
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
        if (statusListener != null && Flagship.getStatus() != Flagship.Status.READY)
            statusListener.onStatusChanged(Flagship.Status.READY);
    }

    public void stopPolling() {
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
    }
}
