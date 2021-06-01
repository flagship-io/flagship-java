package com.abtasty.flagship.decision;

import com.abtasty.flagship.api.HttpManager;
import com.abtasty.flagship.api.Response;
import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.main.FlagshipConfig;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import com.abtasty.flagship.model.Variation;
import com.abtasty.flagship.model.VariationGroup;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import com.abtasty.flagship.visitor.VisitorDelegate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BucketingManager extends DecisionManager {

    private String                          last_modified;
    private ArrayList<Campaign>             campaigns = new ArrayList<>();
    private ScheduledExecutorService        executor;

    public BucketingManager(FlagshipConfig<?> config) {
        super(config);
        startPolling();
    }

    @Override
    public void setStatusListener(Flagship.StatusListener statusListener) {
        super.setStatusListener(statusListener);
        if (Flagship.getStatus().lessThan(Flagship.Status.READY))
            statusListener.onStatusChanged(Flagship.Status.POLLING);
    }

    public void startPolling() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });
            Runnable runnable = () -> {
                FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING, LogManager.Level.DEBUG,
                        FlagshipConstants.Info.BUCKETING_INTERVAL);
                updateBucketingCampaigns();
            };
            long time = config.getPollingTime();
            TimeUnit unit = config.getPollingUnit();
            if (time == 0)
                executor.execute(runnable);
            else
                executor.scheduleAtFixedRate(runnable, 0, time, unit);
        }
    }

    private void updateBucketingCampaigns() {
        try {
            HashMap<String, String> headers = new HashMap<String, String>();
            if (last_modified != null)
                headers.put("If-Modified-Since", last_modified);
            Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.GET,
                    String.format(BUCKETING, config.getEnvId()), headers, null, config.getTimeout());
            logResponse(response);
            if (response.isSuccess(false)) {
                last_modified = response.getResponseHeader("Last-Modified");
                ArrayList<Campaign> campaigns = parseCampaignsResponse(response.getResponseContent());
                if (campaigns != null)
                    this.campaigns = campaigns;
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

    public void stop() {
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
    }

    @Override
    public HashMap<String, Modification> getCampaignsModifications(VisitorDelegate visitor) {
        try {
            if (campaigns != null) {
                HashMap<String, Modification> campaignsModifications = new HashMap<>();
                for (Campaign campaign : campaigns) {
                    for (VariationGroup variationGroup : campaign.getVariationGroups()) {
                        if (variationGroup.isTargetingValid(new HashMap<>(visitor.getContext()))) {
                            Variation variation = variationGroup.selectVariation(visitor);
                            HashMap<String, Modification> modificationsValues = variation.getModificationsValues();
                            if (modificationsValues != null)
                                campaignsModifications.putAll(modificationsValues);
                            break;
                        }
                    }
                }
                return campaignsModifications;
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.ERROR, e.getMessage());
        }
        return null;
    }
}
