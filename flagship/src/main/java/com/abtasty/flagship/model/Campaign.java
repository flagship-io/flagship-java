package com.abtasty.flagship.model;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

public class Campaign implements Serializable {

    private final String                                    id;
    private final LinkedHashMap<String, VariationGroup>     variationGroups;

    public Campaign(String id, LinkedHashMap<String, VariationGroup> variationGroups) {
        this.id = id;
        this.variationGroups = variationGroups;
    }

    public String getId() {
        return id;
    }

    public LinkedHashMap<String, VariationGroup> getVariationGroups() {
        return variationGroups;
    }

    public HashMap<String, Modification> getModifications(Flagship.Mode mode) {
        HashMap<String, Modification> modifications = new HashMap<>();
        this.variationGroups.forEach((key, variationGroup) -> {
            if (mode == Flagship.Mode.DECISION_API) {
                variationGroup.getVariations().forEach((variationId, variation) -> {
                    modifications.putAll(variation.getModifications().getValues());
                });
            } else {
                //bucketing
            }
        });
        return modifications;
    }

    public static ArrayList<Campaign> parse(JSONArray campaignsArray) {
        try {
            ArrayList<Campaign> campaigns = new ArrayList<>();
            campaignsArray.forEach(campaignObject -> {
                Campaign campaign = Campaign.parse((JSONObject) campaignObject);
                if (campaign != null)
                    campaigns.add(campaign);
            });
            return campaigns;
        } catch (Exception e){
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
            return null;
        }
    }

    public static Campaign parse(JSONObject campaignObject) {
        try {
            String id = campaignObject.getString("id");
            LinkedHashMap<String, VariationGroup> variationGroups = new LinkedHashMap<>();
            JSONArray variationGroupArray = campaignObject.optJSONArray("variationGroups");
            if (variationGroupArray != null) {
                //bucketing
                variationGroupArray.forEach(variationGroupsObj -> {
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
            FlagshipLogManager.log(FlagshipLogManager.Tag.PARSING, LogManager.Level.ERROR, FlagshipConstants.Errors.PARSING_CAMPAIGN_ERROR);
            return null;
        }
    }

    @Override
    public String toString() {
        return "Campaign{" +
                "id='" + id + '\'' +
                ", variationGroups=" + variationGroups +
                '}';
    }
}

