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
import com.abtasty.flagship.visitor.VisitorDelegateDTO;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BucketingManager extends DecisionManager {

    private final String                    LOCAL_DECISION_FILE_NAME = "local_decision_file.json";
    private String                          lastModified;
    private String                          localDecisionFile;
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
            if (lastModified != null)
                headers.put("If-Modified-Since", lastModified);
            Response response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.GET,
                    String.format(BUCKETING, config.getEnvId()), headers, null, config.getTimeout());
            logResponse(response);
            if (response.isSuccess(false)) {
                lastModified = response.getResponseHeader("Last-Modified");
                ArrayList<Campaign> campaigns = parseCampaignsResponse(response.getResponseContent());
                if (campaigns != null)
                    this.campaigns = campaigns;
//                updateFlagshipStatus(isPanic() ? Flagship.Status.PANIC : Flagship.Status.READY);
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.ERROR, e.getMessage());
        }
    }

    public void stop() {
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
    }

    @Override
    public HashMap<String, Modification> getCampaignsModifications(VisitorDelegateDTO visitor) {
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
                visitor.getVisitorDelegate().getStrategy().sendContextRequest();
                return campaignsModifications;
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.SYNCHRONIZE, LogManager.Level.ERROR, e.getMessage());
        }
        return null;
    }

    public void saveLocalDecisionFile() {
        if (lastModified != null && localDecisionFile != null) {
            try {
                JSONObject localDecisionJson = new JSONObject()
                        .put("last_modified", lastModified)
                        .put("local_decision_file", localDecisionFile);
                FileWriter fWriter = new FileWriter(LOCAL_DECISION_FILE_NAME, false);
                BufferedWriter fOut = new BufferedWriter(fWriter);
                fOut.write(localDecisionJson.toString(4));
                fWriter.close();
                fOut.close();
            } catch (Exception e) {

            }
        }
    }

    public void loadLocalDecisionFile() {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(LOCAL_DECISION_FILE_NAME));
            String line = null;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            reader.close();
            String content = builder.toString();
            if (!content.isEmpty()) {
                JSONObject localDecisionJson = new JSONObject();
                lastModified = localDecisionJson.optString("last_modified", null);
                localDecisionFile = localDecisionJson.optString("local_decision_file", null);
            }
        } catch (Exception e) {

        }
    }
}
