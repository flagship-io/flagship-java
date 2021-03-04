package com.abtasty.flagship.decision;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;

import java.util.ArrayList;
import java.util.HashMap;

public interface IDecisionManager {

    public ArrayList<Campaign> getCampaigns(String envId, String visitorId, HashMap<String, Object> context);
    public HashMap<String, Modification> getModifications(Flagship.Mode mode, ArrayList<Campaign> campaigns);
}
