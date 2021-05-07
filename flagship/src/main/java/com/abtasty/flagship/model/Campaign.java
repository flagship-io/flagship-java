package com.abtasty.flagship.model;

import com.abtasty.flagship.utils.FlagshipConstants;
import com.abtasty.flagship.utils.FlagshipLogManager;
import com.abtasty.flagship.utils.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Campaign implements Serializable {

    private final String                                    id;
    private final LinkedHashMap<String, VariationGroup>     variationGroups;
    private String                                          selectedVariationGroupId;

    public Campaign(String id, LinkedHashMap<String, VariationGroup> variationGroups, String selectedVariationGroupId) {
        this.id = id;
        this.variationGroups = variationGroups;
        this.selectedVariationGroupId = selectedVariationGroupId;
    }

    public String getId() {
        return id;
    }

    public LinkedHashMap<String, VariationGroup> getVariationGroups() {
        return variationGroups;
    }

    public void selectVariation(String visitorId) {
        for (Map.Entry<String, VariationGroup> e : variationGroups.entrySet()) {
            VariationGroup variationGroup = e.getValue();
            variationGroup.selectVariation(visitorId);
        }
    }

    public boolean selectedVariationGroupFromTargeting(HashMap<String, Object> context) {
        for (Map.Entry<String, VariationGroup> e : variationGroups.entrySet()) {
            VariationGroup variationGroup = e.getValue();
            if (variationGroup.isTargetingValid(context)) {
                selectedVariationGroupId = variationGroup.getVariationGroupId();
                return true;
            }
        }
        return false;
    }

    public HashMap<String, Modification> getModifications() {
        HashMap<String, Modification> modifications = new HashMap<>();
        VariationGroup selectedVariationGroup = this.getSelectedVariationGroup();
        if (selectedVariationGroup != null) {
            Variation selectedVariation = selectedVariationGroup.getSelectedVariation();
            if (selectedVariation != null)
                modifications.putAll(selectedVariation.getModifications().getValues());
        }
        return modifications;
    }

    public VariationGroup getSelectedVariationGroup() {
        if (selectedVariationGroupId != null && variationGroups != null)
            return variationGroups.get(selectedVariationGroupId);
        return null;
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
            String selectedVariationGroup = null;
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
                if (variationGroup != null) {
                    selectedVariationGroup = variationGroup.getVariationGroupId();
                    variationGroups.put(variationGroup.getVariationGroupId(), variationGroup);
                }
            }
            return new Campaign(id, variationGroups, selectedVariationGroup);
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

    @Override
    public Campaign clone() {
        try {
            return (Campaign) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

