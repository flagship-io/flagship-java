package com.abtasty.flagship.model;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.LogLevel;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class Campaign implements Serializable {

    private String          id;
    private LinkedHashMap   variationGroups = new LinkedHashMap<String, VariationGroup>();

    public Campaign(String id, LinkedHashMap variationGroups) {
        this.id = id;
        this.variationGroups = variationGroups;
    }

    public String getId() {
        return id;
    }

    public LinkedHashMap getVariationGroups() {
        return variationGroups;
    }

    public HashMap<String, Modification> getModifications(Flagship.Mode mode) {
        HashMap<String, Modification> modifications = new HashMap<>();
        Iterator entries = this.variationGroups.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, VariationGroup> entry = (Map.Entry<String, VariationGroup>) entries.next();
            if (mode == Flagship.Mode.DECISION_API) {
                entry.getValue().getVariations().forEach((variationId, variation) -> {
                    System.out.println("variation : " + variation);
                });
            } else {
                //bucketing
            }
        }
        return modifications;
    }

    public static ArrayList<Campaign> parse(String json) {
        try {
            JSONObject main = new JSONObject(json);
            ArrayList<Campaign> campaigns = new ArrayList<>();
            JSONArray campaignArray = main.getJSONArray("campaigns");
            campaignArray.forEach(campaignObject -> {
                Campaign campaign = Campaign.parse((JSONObject) campaignObject);
                if (campaign != null)
                    campaigns.add(campaign);
            });
            return campaigns;
        } catch (Exception e){
            Flagship.getConfig().logManager.onLog(LogManager.Tag.PARSING, LogLevel.ERROR, FlagshipConstants.PARSING_CAMPAIGN_ERROR);
            e.printStackTrace();
            return null;
        }
    }

    public static Campaign parse(JSONObject campaignObject) {
        try {
            String id = campaignObject.getString("id");
            LinkedHashMap<String, VariationGroup> variationGroups = new LinkedHashMap<>();
            JSONArray variationGroupdArr = campaignObject.optJSONArray("variationGroups");
            if (variationGroupdArr != null) {
                //bucketing
                variationGroupdArr.forEach(variationGroupsObj -> {
                    VariationGroup variationGroup = VariationGroup.parse(id, (JSONObject) variationGroupsObj, true);
                    if (variationGroup != null)
                        variationGroups.put(variationGroup.getVariationGroupId(), variationGroup);
                });
            } else {
                //api
                VariationGroup variationGroup = VariationGroup.parse(id, campaignObject, false);
                if (variationGroup != null)
                    variationGroups.put(variationGroup.getVariationGroupId(), variationGroup);
            }
            return new Campaign(id, variationGroups);
        }
        catch (Exception e){
            Flagship.getConfig().logManager.onLog(LogManager.Tag.PARSING, LogLevel.ERROR, FlagshipConstants.PARSING_CAMPAIGN_ERROR);
            return null;
        }
    }
}

