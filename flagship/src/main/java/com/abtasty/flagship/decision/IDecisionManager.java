package com.abtasty.flagship.decision;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.model.Campaign;
import com.abtasty.flagship.model.Modification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

public interface IDecisionManager {

    ArrayList<Campaign> getCampaigns(String envId, String visitorId, ConcurrentMap<String, Object> context);
    HashMap<String, Modification> getModifications(Flagship.Mode mode, ArrayList<Campaign> campaigns);
}
