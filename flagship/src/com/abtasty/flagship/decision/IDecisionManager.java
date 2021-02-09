package com.abtasty.flagship.decision;

import com.abtasty.flagship.model.Campaign;

import java.util.ArrayList;
import java.util.HashMap;

public interface IDecisionManager {

    public ArrayList<Campaign> getCampaigns(String visitorId, HashMap<String, Object> context);
    public void getModifications();
}
