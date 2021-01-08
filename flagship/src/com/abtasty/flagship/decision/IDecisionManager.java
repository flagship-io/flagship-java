package com.abtasty.flagship.decision;

import java.util.HashMap;

public interface IDecisionManager {
    public void getCampaigns(String visitorId, HashMap<String, Object> context);
    public void getModifications();
}
