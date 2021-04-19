package com.abtasty.flagship.decision;

import com.abtasty.flagship.model.Campaign;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class BucketingManager extends DecisionManager {

    private long        time = 60;
    private TimeUnit    unit = TimeUnit.SECONDS;

    @Override
    public ArrayList<Campaign> getCampaigns(String envId, String visitorId, ConcurrentMap<String, Object> context) {
        return null;
    }




    public void setPollingInterval(long time, TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }
}
