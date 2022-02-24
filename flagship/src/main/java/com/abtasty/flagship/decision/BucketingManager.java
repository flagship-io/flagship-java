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

import static com.abtasty.flagship.utils.FlagshipConstants.Errors.BUCKETING_POLLING_ERROR;

public class BucketingManager extends DecisionManager {

    private final String                    LOCAL_DECISION_FILE_NAME = "local_decision_file.json";
    private final String                    LOCAL_DECISION_FILE = "local_decision_file";
    private final String                    LAST_MODIFIED_LOCAL_DECISION_FILE = "last_modified";
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
            loadLocalDecisionFile();
            if (lastModified != null) headers.put("If-Modified-Since", lastModified);
            Response response = null;
            try {
                response = HttpManager.getInstance().sendHttpRequest(HttpManager.RequestType.GET,
                    String.format(BUCKETING, config.getEnvId()), headers, null, config.getTimeout());
            } catch (Exception e) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING, LogManager.Level.ERROR, String.format(BUCKETING_POLLING_ERROR, e.getMessage() != null ? e.getMessage() : ""));
                if (localDecisionFile != null)
                    FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING, LogManager.Level.INFO, String.format(FlagshipConstants.Info.BUCKETING_CACHE,
                            lastModified, new JSONObject(localDecisionFile).toString(4)));
            }
            if (response != null) {
                logResponse(response);
                if (response.getResponseCode() < 300) {
                    lastModified = response.getResponseHeader("Last-Modified");
                    localDecisionFile = response.getResponseContent();
                    saveLocalDecisionFile();
                }
            }
            parseLocalDecisionFile();
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.FETCHING, LogManager.Level.ERROR, e.getMessage() != null ? e.getMessage() : "");
        }
        updateFlagshipStatus(isPanic() ? Flagship.Status.PANIC : Flagship.Status.READY);
    }

    private void parseLocalDecisionFile() {
        if (localDecisionFile != null) {
            ArrayList<Campaign> campaigns = parseCampaignsResponse(localDecisionFile);
            if (campaigns != null)
                this.campaigns = campaigns;
        }
    }


    public void stop() {
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
        executor = null;
    }

    @Override
    public HashMap<String, Modification> getCampaignsModifications(VisitorDelegateDTO visitorDelegateDTO) {
        try {
            if (campaigns != null) {
                HashMap<String, Modification> campaignsModifications = new HashMap<>();
                for (Campaign campaign : campaigns) {
                    for (VariationGroup variationGroup : campaign.getVariationGroups()) {
                        if (variationGroup.isTargetingValid(new HashMap<>(visitorDelegateDTO.getContext()))) {
                            Variation variation = variationGroup.selectVariation(visitorDelegateDTO);
                            if (variation != null) {
                                visitorDelegateDTO.addNewAssignmentToHistory(variation.getVariationGroupId(), variation.getVariationId());
                                HashMap<String, Modification> modificationsValues = variation.getModificationsValues();
                                if (modificationsValues != null)
                                    campaignsModifications.putAll(modificationsValues);
                                break;
                            }
                        }
                    }
                }
                visitorDelegateDTO.getVisitorDelegate().getStrategy().sendContextRequest();
                return campaignsModifications;
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.FETCHING, LogManager.Level.ERROR, (e.getMessage() != null) ? e.getMessage() : "");
        }
        return null;
    }

    public void saveLocalDecisionFile() {
        if (lastModified != null && localDecisionFile != null) {
            try {
                JSONObject localDecisionJson = new JSONObject()
                        .put(LAST_MODIFIED_LOCAL_DECISION_FILE, lastModified)
                        .put(LOCAL_DECISION_FILE, new JSONObject(localDecisionFile));
                FileWriter fWriter = new FileWriter(LOCAL_DECISION_FILE_NAME, false);
                BufferedWriter fOut = new BufferedWriter(fWriter);
                fOut.write(localDecisionJson.toString());
                fOut.close();
                fWriter.close();
            } catch (Exception e) {
                FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.BUCKETING_SAVING_ERROR,( e.getMessage() != null) ? e.getMessage() : ""));
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
                JSONObject localDecisionJson = new JSONObject(content);
                lastModified = localDecisionJson.optString(LAST_MODIFIED_LOCAL_DECISION_FILE, null);
                localDecisionFile = localDecisionJson.optString(LOCAL_DECISION_FILE, null);
            }
        } catch (Exception e) {
            FlagshipLogManager.log(FlagshipLogManager.Tag.BUCKETING, LogManager.Level.ERROR, String.format(FlagshipConstants.Errors.BUCKETING_LOADING_ERROR,( e.getMessage() != null) ? e.getMessage() : ""));
        }
    }
}
