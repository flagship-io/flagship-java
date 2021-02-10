package com.abtasty.flagship.decision;

import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;

import java.util.ArrayList;
import java.util.HashMap;

public interface IDecisionManager {

    public ArrayList<Campaign> getCampaigns(String visitorId, HashMap<String, Object> context);
    public HashMap<String, Modification> getModifications(ArrayList<Campaign> campaigns);
}
